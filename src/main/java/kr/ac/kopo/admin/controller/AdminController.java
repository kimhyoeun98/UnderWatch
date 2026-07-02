package kr.ac.kopo.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.ac.kopo.board.service.BoardService;
import kr.ac.kopo.board.vo.BoardCategoryVO;
import kr.ac.kopo.board.vo.SearchVO;
import kr.ac.kopo.comment.service.CommentService;
import kr.ac.kopo.board.vo.BoardVO;
import kr.ac.kopo.member.service.MemberService;
import kr.ac.kopo.message.service.MessageService;
import kr.ac.kopo.notification.service.NotificationService;
import kr.ac.kopo.report.service.ReportService;
import kr.ac.kopo.report.vo.ReportVO;
import kr.ac.kopo.visit.service.VisitService;

/**
 * A-01~A-02 관리자 대시보드. URL /admin/** 은 spring-security.xml에서 hasRole('ADMIN').
 */
@Controller
public class AdminController {

	@Autowired
	private MemberService memberService;

	@Autowired
	private BoardService boardService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private MessageService messageService;

	@Autowired
	private VisitService visitService;

	@Autowired
	private NotificationService notificationService;

	/**
	 * 대시보드
	 * GET /admin
	 */
	@GetMapping({"/admin", "/admin/"})
	public String dashboard(Model model) {
		SearchVO s = new SearchVO();
		boardService.search(s);
		model.addAttribute("memberCount", memberService.getAllMembers().size());
		model.addAttribute("postCount", s.getTotalCount());
		model.addAttribute("categoryCount", boardService.getCategories().size());
		return "admin/dashboard";
	}

	/**
	 * A-01 회원 관리
	 * GET /admin/members
	 */
	@GetMapping("/admin/members")
	public String members(Model model) {
		model.addAttribute("members", memberService.getAllMembers());
		return "admin/members";
	}

	/**
	 * A-01 회원 상태 변경
	 * POST /admin/members/status
	 */
	@PostMapping("/admin/members/status")
	public String changeStatus(@RequestParam("id") String id,
							   @RequestParam("status") String status,
							   @AuthenticationPrincipal UserDetails me,
							   RedirectAttributes ra) {
		if (me.getUsername().equals(id)) {
			ra.addFlashAttribute("error", "본인 계정은 변경할 수 없습니다.");
			return "redirect:/admin/members";
		}
		memberService.changeStatus(id, status);
		// M-11 차단 알림: 정지 처리 시 대상자에게 알림
		if ("SUSPENDED".equals(status)) {
			notificationService.notify(id, "회원님의 계정이 관리자에 의해 정지되었습니다.", "/member/mypage");
		}
		return "redirect:/admin/members";
	}

	/**
	 * A-01 회원 권한 변경
	 * POST /admin/members/role
	 */
	@PostMapping("/admin/members/role")
	public String changeRole(@RequestParam("id") String id,
							 @RequestParam("role") String role,
							 @AuthenticationPrincipal UserDetails me,
							 RedirectAttributes ra) {
		if (me.getUsername().equals(id)) {
			ra.addFlashAttribute("error", "본인 계정은 변경할 수 없습니다.");
			return "redirect:/admin/members";
		}
		memberService.changeRole(id, role);
		return "redirect:/admin/members";
	}

	/**
	 * A-02 게시판(카테고리) 관리
	 * GET /admin/categories
	 */
	@GetMapping("/admin/categories")
	public String categories(Model model) {
		model.addAttribute("categories", boardService.getCategories());
		return "admin/categories";
	}

	/**
	 * A-02 카테고리 추가
	 * POST /admin/categories/add
	 */
	@PostMapping("/admin/categories/add")
	public String addCategory(@ModelAttribute BoardCategoryVO category) {
		boardService.addCategory(category);
		return "redirect:/admin/categories";
	}

	/**
	 * A-02 카테고리 수정
	 * POST /admin/categories/edit
	 */
	@PostMapping("/admin/categories/edit")
	public String editCategory(@ModelAttribute BoardCategoryVO category) {
		boardService.editCategory(category);
		return "redirect:/admin/categories";
	}

	/**
	 * A-02 카테고리 삭제
	 * POST /admin/categories/delete
	 */
	@PostMapping("/admin/categories/delete")
	public String deleteCategory(@RequestParam("no") int no, RedirectAttributes ra) {
		try {
			boardService.removeCategory(no);
		} catch (Exception e) {
			ra.addFlashAttribute("error", "게시글이 있는 카테고리는 삭제할 수 없습니다.");
		}
		return "redirect:/admin/categories";
	}

	/**
	 * A-03 신고 목록
	 * GET /admin/reports
	 */
	@GetMapping("/admin/reports")
	public String reports(Model model) {
		model.addAttribute("reports", reportService.getReports());
		return "admin/reports";
	}

	/**
	 * A-03 신고 처리(블라인드)
	 * POST /admin/reports/blind
	 */
	@PostMapping("/admin/reports/blind")
	public String blind(@RequestParam("no") int no,
						@RequestParam("targetType") String targetType,
						@RequestParam("targetNo") int targetNo) {
		// M-11 차단 알림: 블라인드 전에 콘텐츠 작성자를 파악해 알림
		String ownerId;
		String link;
		if ("B".equals(targetType)) {
			BoardVO board = boardService.getDetail(targetNo);
			ownerId = (board == null) ? null : board.getWriterId();
			link = "/board/detail?no=" + targetNo;
			boardService.delete(targetNo);     // 게시글 논리삭제
		} else {
			ownerId = commentService.getWriterId(targetNo);
			link = "/notification";
			commentService.blind(targetNo);    // 댓글 논리삭제
		}
		if (ownerId != null) {
			notificationService.notify(ownerId,
				"신고 처리로 회원님의 " + ("B".equals(targetType) ? "게시글" : "댓글") + "이 블라인드 처리되었습니다.",
				link);
		}
		reportService.resolve(no);
		return "redirect:/admin/reports";
	}

	/**
	 * A-03 신고 단순 종결(블라인드 없이) — 쪽지 신고 등 콘텐츠 삭제가 불가한 경우
	 * POST /admin/reports/resolve
	 */
	@PostMapping("/admin/reports/resolve")
	public String resolveReport(@RequestParam("no") int no) {
		reportService.resolve(no);
		return "redirect:/admin/reports";
	}

	/**
	 * R-03 쪽지 신고 대화 내용 조회. 신고 사유에 기록된 상대 아이디를 파싱해
	 * 신고자와 상대 간 전체 대화(삭제 여부 무관)를 관리자에게 보여준다.
	 * GET /admin/reports/message?no={reportNo}
	 */
	@GetMapping("/admin/reports/message")
	public String reportMessage(@RequestParam("no") int no, Model model, RedirectAttributes ra) {
		ReportVO report = reportService.getReport(no);
		if (report == null || !"M".equals(report.getTargetType())) {
			ra.addFlashAttribute("reportMsg", "쪽지 신고가 아니거나 존재하지 않는 신고입니다.");
			return "redirect:/admin/reports";
		}
		String partnerId = extractPartnerId(report.getReason());
		if (partnerId == null) {
			ra.addFlashAttribute("reportMsg", "신고에서 상대 아이디를 찾을 수 없습니다.");
			return "redirect:/admin/reports";
		}
		model.addAttribute("report", report);
		model.addAttribute("reporterId", report.getReporterId());
		model.addAttribute("partnerId", partnerId);
		model.addAttribute("messages", messageService.getThreadForAdmin(report.getReporterId(), partnerId));
		return "admin/reportMessage";
	}

	/** 신고 사유의 "[쪽지 상대: id] ..." 패턴에서 상대 아이디를 추출한다. */
	private String extractPartnerId(String reason) {
		if (reason == null) {
			return null;
		}
		Matcher m = Pattern.compile("\\[쪽지 상대: ([^\\]]+)\\]").matcher(reason);
		return m.find() ? m.group(1).trim() : null;
	}

	/**
	 * A-04 방문자 통계
	 * GET /admin/stats
	 */
	@GetMapping("/admin/stats")
	public String stats(Model model) {
		model.addAttribute("visits", visitService.getRecent());
		return "admin/stats";
	}
}
