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
						 @RequestParam(value = "targetNo", required = false, defaultValue = "0") int targetNo,
						 @RequestParam(value = "boardNo", required = false) Integer boardNo,
						 @RequestParam(value = "partnerId", required = false) String partnerId,
						 @RequestParam(value = "reason", required = false) String reason,
						 @AuthenticationPrincipal UserDetails userDetails,
						 RedirectAttributes ra) {
		// R-03 쪽지 신고: 신고 대상(상대 아이디)을 사유에 함께 기록
		String finalReason = reason;
		if ("M".equals(targetType) && partnerId != null && !partnerId.isBlank()) {
			finalReason = "[쪽지 상대: " + partnerId + "] " + (reason == null ? "" : reason);
		}
		boolean ok = reportService.report(targetType, targetNo, userDetails.getUsername(), finalReason);
		ra.addFlashAttribute("reportMsg", ok ? "신고가 접수되었습니다." : "이미 신고한 대상입니다.");
		if ("M".equals(targetType)) {
			return "redirect:/message";
		}
		return "redirect:/board/detail?no=" + boardNo;
	}
}
