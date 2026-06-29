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

import kr.ac.kopo.board.service.BoardService;
import kr.ac.kopo.board.vo.BoardCategoryVO;
import kr.ac.kopo.board.vo.SearchVO;
import kr.ac.kopo.comment.service.CommentService;
import kr.ac.kopo.member.service.MemberService;
import kr.ac.kopo.report.service.ReportService;
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
	private VisitService visitService;

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
		if ("B".equals(targetType)) {
			boardService.delete(targetNo);     // 게시글 논리삭제
		} else {
			commentService.blind(targetNo);    // 댓글 논리삭제
		}
		reportService.resolve(no);
		return "redirect:/admin/reports";
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
