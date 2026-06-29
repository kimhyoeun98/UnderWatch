package kr.ac.kopo.report.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.ac.kopo.report.service.ReportService;

@Controller
public class ReportController {

	@Autowired
	private ReportService reportService;

	/**
	 * 게시글/댓글 신고 (로그인 필요). 신고 후 해당 글 상세로 복귀
	 * POST /report
	 */
	@PostMapping("/report")
	public String report(@RequestParam("targetType") String targetType,
						 @RequestParam("targetNo") int targetNo,
						 @RequestParam("boardNo") int boardNo,
						 @RequestParam(value = "reason", required = false) String reason,
						 @AuthenticationPrincipal UserDetails userDetails,
						 RedirectAttributes ra) {
		boolean ok = reportService.report(targetType, targetNo, userDetails.getUsername(), reason);
		ra.addFlashAttribute("reportMsg", ok ? "신고가 접수되었습니다." : "이미 신고한 대상입니다.");
		return "redirect:/board/detail?no=" + boardNo;
	}
}
