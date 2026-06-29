package kr.ac.kopo.member.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kr.ac.kopo.board.service.BoardService;
import kr.ac.kopo.board.vo.BoardVO;
import kr.ac.kopo.comment.service.CommentService;
import kr.ac.kopo.member.security.MemberDetailsService;
import kr.ac.kopo.member.service.MemberService;
import kr.ac.kopo.member.vo.MemberVO;

@Controller
public class MemberController {

	private static final String UPLOAD_DIR = "${user.home}/underwatch/uploads";

	@Autowired
	private MemberService memberService;

	@Autowired
	private BoardService boardService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private MemberDetailsService memberDetailsService;

	/**
	 * 로그인폼
	 * GET /member/login
	 */
	@GetMapping("/member/login")
	public String loginForm(@RequestParam(name = "error", required = false) String error, Model model) {
		if (error != null) {
			model.addAttribute("errorMsg", "아이디 또는 비밀번호가 올바르지 않습니다.");
		}
		return "member/login";
	}

	/**
	 * 회원가입폼
	 * GET /member/register
	 */
	@GetMapping("/member/register")
	public String registerForm(Model model) {
		model.addAttribute("memberVO", new MemberVO());
		return "member/register";
	}

	/**
	 * 회원가입처리
	 * POST /member/register
	 */
	@PostMapping("/member/register")
	public String register(@Valid @ModelAttribute MemberVO memberVO,
						   BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "member/register";
		}
		if (memberService.isIdDuplicate(memberVO.getId())) {
			model.addAttribute("idError", "이미 사용 중인 아이디입니다.");
			return "member/register";
		}
		if (memberService.isNicknameDuplicate(memberVO.getNickname())) {
			model.addAttribute("nicknameError", "이미 사용 중인 닉네임입니다.");
			return "member/register";
		}
		if (memberService.isEmailDuplicate(memberVO.getEmail())) {
			model.addAttribute("emailError", "이미 사용 중인 이메일입니다.");
			return "member/register";
		}
		memberService.register(memberVO);
		return "redirect:/member/login?registered=true";
	}

	/**
	 * 아이디 중복 체크 (AJAX)
	 * GET /member/idCheck
	 */
	@GetMapping("/member/idCheck")
	@ResponseBody
	public String idCheck(@RequestParam("id") String id) {
		return memberService.isIdDuplicate(id) ? "DUPLICATE" : "OK";
	}

	/**
	 * 닉네임 중복 체크 (AJAX)
	 * GET /member/nicknameCheck
	 */
	@GetMapping("/member/nicknameCheck")
	@ResponseBody
	public String nicknameCheck(@RequestParam("nickname") String nickname) {
		return memberService.isNicknameDuplicate(nickname) ? "DUPLICATE" : "OK";
	}

	/**
	 * 이메일 중복 체크 (AJAX)
	 * GET /member/emailCheck
	 */
	@GetMapping("/member/emailCheck")
	@ResponseBody
	public String emailCheck(@RequestParam("email") String email) {
		return memberService.isEmailDuplicate(email) ? "DUPLICATE" : "OK";
	}

	/**
	 * 마이페이지
	 * GET /member/mypage
	 */
	@GetMapping("/member/mypage")
	public String mypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		String id = userDetails.getUsername();
		model.addAttribute("member", memberService.findById(id));

		List<BoardVO> myPosts = boardService.getMyPosts(id);
		model.addAttribute("myPosts", myPosts);
		model.addAttribute("myComments", commentService.getMyComments(id));

		// M-08 등급: 글수*10 + 받은추천*5 + 받은조회*1
		int postCnt = myPosts.size();
		int likeSum = myPosts.stream().mapToInt(BoardVO::getLikeCnt).sum();
		int viewSum = myPosts.stream().mapToInt(BoardVO::getViewCnt).sum();
		int score = postCnt * 10 + likeSum * 5 + viewSum;
		model.addAttribute("gradeScore", score);
		model.addAttribute("grade", gradeOf(score));
		model.addAttribute("hasFace", memberService.hasFace(id));   // M-09
		return "member/mypage";
	}

	/* M-08 활동점수 -> 등급 */
	private String gradeOf(int score) {
		if (score >= 500) return "다이아몬드";
		if (score >= 300) return "플래티넘";
		if (score >= 150) return "골드";
		if (score >= 50)  return "실버";
		return "브론즈";
	}

	/**
	 * 회원정보 수정
	 * POST /member/mypage/info
	 */
	@PostMapping("/member/mypage/info")
	public String updateInfo(@AuthenticationPrincipal UserDetails userDetails,
							 @RequestParam("nickname") String nickname,
							 @RequestParam("phone") String phone,
							 @RequestParam("email") String email,
							 RedirectAttributes ra) {
		String id = userDetails.getUsername();
		MemberVO original = memberService.findById(id);

		// M-07: 닉네임을 바꾸려는 경우에만 중복/주기 검사
		boolean nickChanged = !original.getNickname().equals(nickname);
		if (nickChanged) {
			if (memberService.isNicknameDuplicate(nickname)) {
				ra.addFlashAttribute("infoError", "이미 사용 중인 닉네임입니다.");
				return "redirect:/member/mypage";
			}
			if (!memberService.canChangeNickname(id)) {
				ra.addFlashAttribute("infoError", "닉네임은 30일마다 변경할 수 있습니다.");
				return "redirect:/member/mypage";
			}
		}

		MemberVO member = new MemberVO();
		member.setId(id);
		member.setNickname(nickname);
		member.setPhone(phone);
		member.setEmail(email);
		memberService.updateInfo(member);
		if (nickChanged) {
			memberService.markNicknameChanged(id);
		}
		return "redirect:/member/mypage?updated=true";
	}

	/**
	 * 비밀번호 변경
	 * POST /member/mypage/password
	 */
	@PostMapping("/member/mypage/password")
	public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
								 @RequestParam("currentPassword") String currentPassword,
								 @RequestParam("newPassword") String newPassword,
								 @RequestParam("confirmPassword") String confirmPassword,
								 Model model) {
		MemberVO member = memberService.findById(userDetails.getUsername());

		if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
			model.addAttribute("member", member);
			model.addAttribute("pwError", "현재 비밀번호가 올바르지 않습니다.");
			return "member/mypage";
		}
		if (!newPassword.equals(confirmPassword)) {
			model.addAttribute("member", member);
			model.addAttribute("pwError", "새 비밀번호가 일치하지 않습니다.");
			return "member/mypage";
		}
		memberService.changePassword(userDetails.getUsername(), newPassword);
		return "redirect:/member/mypage?pwChanged=true";
	}

	/**
	 * M-06 프로필 사진 업로드
	 * POST /member/mypage/profile
	 */
	@PostMapping("/member/mypage/profile")
	public String uploadProfile(@AuthenticationPrincipal UserDetails userDetails,
								@RequestParam("profileFile") MultipartFile file,
								RedirectAttributes ra) {
		if (file != null && !file.isEmpty()) {
			String name = file.getOriginalFilename();
			String ext = (name != null && name.contains(".")) ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
			if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png")) {
				ra.addFlashAttribute("profileError", "프로필 이미지는 jpg, png 파일만 가능합니다.");
				return "redirect:/member/mypage";
			}
			try {
				String stored = UUID.randomUUID().toString() + "." + ext;
				File dir = new File(UPLOAD_DIR);
				if (!dir.exists()) dir.mkdirs();
				file.transferTo(new File(dir, stored));
				memberService.updateProfileImg(userDetails.getUsername(), stored);
			} catch (IOException e) {
				ra.addFlashAttribute("profileError", "업로드 중 오류가 발생했습니다.");
			}
		}
		return "redirect:/member/mypage";
	}

	/**
	 * M-09 얼굴 등록 — 마이페이지에서 본인 얼굴 특징벡터 저장
	 * POST /member/face/register
	 */
	@PostMapping("/member/face/register")
	@ResponseBody
	public Map<String, Object> registerFace(@AuthenticationPrincipal UserDetails userDetails,
											@RequestParam("image") String image) {
		if (userDetails == null) {
			return Map.of("success", false, "message", "로그인이 필요합니다.");
		}
		boolean ok = memberService.saveFace(userDetails.getUsername(), image);
		return ok
				? Map.of("success", true, "message", "얼굴이 등록되었습니다.")
				: Map.of("success", false, "message", "얼굴을 인식하지 못했습니다. 다시 시도해 주세요.");
	}

	/**
	 * M-09 얼굴 로그인 화면(웹캠)
	 * GET /member/face/login
	 */
	@GetMapping("/member/face/login")
	public String faceLoginForm() {
		return "member/faceLogin";
	}

	/**
	 * M-09 얼굴만으로 로그인(1:N) — 일치 회원 찾으면 세션 로그인 처리
	 * POST /member/face/loginProc
	 */
	@PostMapping("/member/face/loginProc")
	@ResponseBody
	public Map<String, Object> faceLoginProc(@RequestParam("image") String image,
											 HttpServletRequest request) {
		String matchedId = memberService.matchFace(image);
		if (matchedId == null) {
			return Map.of("success", false, "message", "일치하는 얼굴을 찾지 못했습니다.");
		}

		// Spring Security 세션 로그인 수동 처리(폼/소셜과 동일한 principal 사용)
		UserDetails principal = memberDetailsService.loadUserByUsername(matchedId);
		UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(auth);
		SecurityContextHolder.setContext(context);

		HttpSession session = request.getSession(true);
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

		return Map.of("success", true, "redirect", request.getContextPath() + "/");
	}
}
