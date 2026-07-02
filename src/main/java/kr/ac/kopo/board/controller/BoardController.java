package kr.ac.kopo.board.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ac.kopo.board.service.BoardService;
import kr.ac.kopo.board.vo.BoardVO;
import kr.ac.kopo.board.vo.SearchVO;
import kr.ac.kopo.comment.service.CommentService;
import kr.ac.kopo.member.service.MemberService;
import kr.ac.kopo.notification.service.NotificationService;

@Controller
public class BoardController {

	/** B-04: 업로드 파일 저장 경로 (DB에는 파일명만, 실제 파일은 디스크) */
	private static final String UPLOAD_DIR = "D:/Serv/ServM/uploads/";

	@Autowired
	private BoardService boardService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private MemberService memberService;

	@Autowired
	private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

	/**
	 * 목록 (페이징 + 검색)
	 * GET /board/list
	 */
	@GetMapping("/board/list")
	public String list(@ModelAttribute SearchVO searchVO, Model model) {
		boardService.search(searchVO);
		model.addAttribute("list", boardService.getList(searchVO));
		model.addAttribute("search", searchVO);
		model.addAttribute("categories", boardService.getCategories());
		return "board/list";
	}

	/**
	 * 상세
	 * GET /board/detail?no=3
	 */
	@GetMapping("/board/detail")
	public String detail(@RequestParam("no") int no,
						 @ModelAttribute SearchVO searchVO,
						 @AuthenticationPrincipal UserDetails userDetails,
						 Model model,
						 HttpServletRequest request,
						 HttpServletResponse response) {
		BoardVO board = boardService.getDetail(no);
		if (board == null || "Y".equals(board.getIsDeleted())) {
			return "redirect:/board/list";
		}
		// I-04: 동일 사용자 중복 조회수 방지 (쿠키, 24시간)
		if (!alreadyViewed(request, response, no)) {
			boardService.increaseView(no);
			board.setViewCnt(board.getViewCnt() + 1);
		}
		model.addAttribute("board", board);
		model.addAttribute("comments", commentService.getComments(no));
		model.addAttribute("search", searchVO);
		model.addAttribute("isAdmin", isAdmin(userDetails));
		model.addAttribute("myVote",
			userDetails != null ? boardService.getMyVote(no, userDetails.getUsername()) : null);
		return "board/detail";
	}

	/**
	 * 작성폼
	 * GET /board/write
	 */
	@GetMapping("/board/write")
	public String writeForm(Model model) {
		model.addAttribute("boardVO", new BoardVO());
		model.addAttribute("categories", boardService.getCategories());
		return "board/write";
	}

	/**
	 * 작성처리
	 * POST /board/write
	 */
	@PostMapping("/board/write")
	public String write(@AuthenticationPrincipal UserDetails userDetails,
						@Valid @ModelAttribute BoardVO boardVO,
						BindingResult result,
						@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
						@RequestParam(value = "guestName", required = false) String guestName,
						@RequestParam(value = "guestPassword", required = false) String guestPassword,
						Model model) {
		if (result.hasErrors()) {
			model.addAttribute("categories", boardService.getCategories());
			return "board/write";
		}
		// B-02: 공지(관리자 전용) 카테고리는 ADMIN만
		if (isAdminOnlyCategory(boardVO.getCategoryNo()) && !isAdmin(userDetails)) {
			model.addAttribute("categories", boardService.getCategories());
			model.addAttribute("errorMsg", "공지사항은 관리자만 작성할 수 있습니다.");
			return "board/write";
		}
		boolean guest = (userDetails == null);
		// B-02: 비로그인 작성자는 표시 이름 + 비밀번호 필수
		if (guest && (guestName == null || guestName.isBlank()
				|| guestPassword == null || guestPassword.isBlank())) {
			model.addAttribute("categories", boardService.getCategories());
			model.addAttribute("errorMsg", "비로그인 작성 시 이름과 비밀번호를 입력하세요.");
			return "board/write";
		}
		// B-04: 이미지 확장자 검증
		if (hasFile(imageFile) && !isAllowedImage(imageFile)) {
			model.addAttribute("categories", boardService.getCategories());
			model.addAttribute("errorMsg", "이미지는 jpg, png 파일만 업로드할 수 있습니다.");
			return "board/write";
		}
		if (guest) {
			boardVO.setWriterId(null);
			boardVO.setGuestName(guestName);
			boardVO.setGuestPassword(passwordEncoder.encode(guestPassword));   // 해시 저장
		} else {
			boardVO.setWriterId(userDetails.getUsername());
		}
		boardService.write(boardVO);   // selectKey로 boardVO.no 채워짐
		storeIfPresent(boardVO.getNo(), imageFile);
		if (!guest) {
			memberService.recalcGrade(boardVO.getWriterId());   // M-08 글 수 반영
		}
		return "redirect:/board/detail?no=" + boardVO.getNo();
	}

	/**
	 * 수정폼
	 * GET /board/edit?no=3
	 */
	@GetMapping("/board/edit")
	public String editForm(@RequestParam("no") int no,
						   @RequestParam(value = "guestPassword", required = false) String guestPassword,
						   @AuthenticationPrincipal UserDetails userDetails,
						   Model model,
						   RedirectAttributes ra) {
		BoardVO board = boardService.getDetail(no);
		if (board == null) {
			return "redirect:/board/list";
		}
		if (board.getWriterId() == null) {
			// B-02 게스트 글: 비밀번호 확인 후 수정 폼 진입
			if (!verifyGuest(no, guestPassword)) {
				ra.addFlashAttribute("editError", "비밀번호가 올바르지 않습니다.");
				return "redirect:/board/detail?no=" + no;
			}
			model.addAttribute("guestPassword", guestPassword);   // 폼 hidden 으로 전달(저장 시 재확인)
		} else {
			// 회원 글: 작성자 본인만
			if (userDetails == null || !board.getWriterId().equals(userDetails.getUsername())) {
				return "redirect:/board/detail?no=" + no;
			}
		}
		model.addAttribute("boardVO", board);
		model.addAttribute("categories", boardService.getCategories());
		return "board/edit";
	}

	/**
	 * 수정처리
	 * POST /board/edit
	 */
	@PostMapping("/board/edit")
	public String edit(@AuthenticationPrincipal UserDetails userDetails,
					   @Valid @ModelAttribute BoardVO boardVO,
					   BindingResult result,
					   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
					   @RequestParam(value = "removeImage", required = false) String removeImage,
					   @RequestParam(value = "guestPassword", required = false) String guestPassword,
					   Model model) {
		if (result.hasErrors()) {
			model.addAttribute("categories", boardService.getCategories());
			return "board/edit";
		}
		BoardVO original = boardService.getDetail(boardVO.getNo());
		if (original == null) {
			return "redirect:/board/list";
		}
		// 권한 검증: 회원 글은 본인, 게스트 글은 비밀번호 일치
		boolean allowed = (original.getWriterId() == null)
				? verifyGuest(boardVO.getNo(), guestPassword)
				: (userDetails != null && original.getWriterId().equals(userDetails.getUsername()));
		if (!allowed) {
			return "redirect:/board/detail?no=" + boardVO.getNo();
		}
		if (isAdminOnlyCategory(boardVO.getCategoryNo()) && !isAdmin(userDetails)) {
			model.addAttribute("categories", boardService.getCategories());
			model.addAttribute("errorMsg", "공지사항 카테고리는 관리자만 사용할 수 있습니다.");
			return "board/edit";
		}
		if (hasFile(imageFile) && !isAllowedImage(imageFile)) {
			model.addAttribute("categories", boardService.getCategories());
			model.addAttribute("errorMsg", "이미지는 jpg, png 파일만 업로드할 수 있습니다.");
			return "board/edit";
		}
		boardService.edit(boardVO);
		if (hasFile(imageFile)) {
			// 새 이미지로 교체 — 기존 파일 삭제 후 저장
			deleteImageFile(original.getImageStored());
			storeIfPresent(boardVO.getNo(), imageFile);
		} else if ("true".equals(removeImage)) {
			// 기존 이미지 삭제만
			deleteImageFile(original.getImageStored());
			boardService.updateImage(boardVO.getNo(), null, null);
		}
		return "redirect:/board/detail?no=" + boardVO.getNo();
	}

	/**
	 * 삭제
	 * POST /board/delete
	 */
	@PostMapping("/board/delete")
	public String delete(@RequestParam("no") int no,
						 @RequestParam(value = "guestPassword", required = false) String guestPassword,
						 @AuthenticationPrincipal UserDetails userDetails) {
		BoardVO board = boardService.getDetail(no);
		if (board == null) {
			return "redirect:/board/list";
		}
		boolean allowed = (board.getWriterId() == null)
				? (verifyGuest(no, guestPassword) || isAdmin(userDetails))   // B-02 게스트 글: 비밀번호 일치 또는 관리자
				: (userDetails != null
						&& (board.getWriterId().equals(userDetails.getUsername()) || isAdmin(userDetails)));
		if (allowed) {
			boardService.delete(no);
		}
		return "redirect:/board/detail?no=" + no;
	}

	/** B-02 게스트 글 비밀번호 검증 (해시 비교) */
	private boolean verifyGuest(int no, String rawPassword) {
		if (rawPassword == null || rawPassword.isBlank()) {
			return false;
		}
		String hash = boardService.getGuestPassword(no);
		return hash != null && passwordEncoder.matches(rawPassword, hash);
	}

	/**
	 * 추천 (I-03)
	 * POST /board/like
	 */
	@PostMapping("/board/like")
	public String like(@RequestParam("no") int no,
					   @AuthenticationPrincipal UserDetails userDetails) {
		boardService.vote(no, userDetails.getUsername(), "L");
		// R-02 알림: 글 작성자에게 (본인 추천이면 생략)
		BoardVO board = boardService.getDetail(no);
		if (board != null && board.getWriterId() != null
				&& !board.getWriterId().equals(userDetails.getUsername())) {
			notificationService.notify(board.getWriterId(),
				userDetails.getUsername() + "님이 회원님의 글을 추천했습니다.",
				"/board/detail?no=" + no);
			memberService.recalcGrade(board.getWriterId());   // M-08 받은 추천 반영
		}
		return "redirect:/board/detail?no=" + no;
	}

	/**
	 * 비추천 (I-03)
	 * POST /board/dislike
	 */
	@PostMapping("/board/dislike")
	public String dislike(@RequestParam("no") int no,
						  @AuthenticationPrincipal UserDetails userDetails) {
		boardService.vote(no, userDetails.getUsername(), "D");
		BoardVO board = boardService.getDetail(no);
		if (board != null && board.getWriterId() != null) {
			memberService.recalcGrade(board.getWriterId());   // M-08 추천수 변동 반영
		}
		return "redirect:/board/detail?no=" + no;
	}

	/**
	 * 이미지 다운로드 (B-04) — 응답 스트림에 직접 출력
	 * GET /board/download?no=3
	 */
	@GetMapping("/board/download")
	public void download(@RequestParam("no") int no, HttpServletResponse response) throws IOException {
		BoardVO board = boardService.getDetail(no);
		if (board == null || board.getImageStored() == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		File file = new File(UPLOAD_DIR, board.getImageStored());
		if (!file.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String filename = URLEncoder.encode(board.getImageOrig(), StandardCharsets.UTF_8).replace("+", "%20");
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
		response.setContentLengthLong(file.length());
		try (FileInputStream in = new FileInputStream(file)) {
			in.transferTo(response.getOutputStream());
		}
	}

	/* ===== 내부 헬퍼 ===== */

	// I-04: 쿠키에 조회한 글 번호를 누적해 중복 조회를 막는다
	private boolean alreadyViewed(HttpServletRequest req, HttpServletResponse res, int no) {
		String token = "-" + no + "-";
		String viewed = "";
		if (req.getCookies() != null) {
			for (Cookie c : req.getCookies()) {
				if ("viewedBoards".equals(c.getName())) { viewed = c.getValue(); break; }
			}
		}
		if (viewed.contains(token)) {
			return true;
		}
		Cookie cookie = new Cookie("viewedBoards", viewed + token);
		cookie.setMaxAge(60 * 60 * 24);
		cookie.setPath("/");
		res.addCookie(cookie);
		return false;
	}

	private boolean isAdminOnlyCategory(int categoryNo) {
		return boardService.getCategories().stream()
				.anyMatch(c -> c.getNo() == categoryNo && "Y".equals(c.getIsAdminOnly()));
	}

	private boolean isAdmin(UserDetails u) {
		return u != null && u.getAuthorities().stream()
				.anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
	}

	// B-04: 파일 유무 / 확장자 검증 / 디스크 저장 / 삭제
	private boolean hasFile(MultipartFile f) {
		return f != null && !f.isEmpty();
	}

	private boolean isAllowedImage(MultipartFile f) {
		String name = f.getOriginalFilename();
		if (name == null || !name.contains(".")) return false;
		String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
		return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png");
	}

	private void storeIfPresent(int boardNo, MultipartFile f) {
		if (!hasFile(f)) return;
		try {
			String name = f.getOriginalFilename();
			String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
			String stored = UUID.randomUUID().toString() + "." + ext;
			File dir = new File(UPLOAD_DIR);
			if (!dir.exists()) dir.mkdirs();
			f.transferTo(new File(dir, stored));
			boardService.updateImage(boardNo, stored, name);
		} catch (IOException e) {
			// 글은 이미 저장됨 — 이미지 저장 실패는 무시(로그성)
		}
	}

	private void deleteImageFile(String stored) {
		if (stored == null) return;
		File f = new File(UPLOAD_DIR, stored);
		if (f.exists()) f.delete();
	}
}
