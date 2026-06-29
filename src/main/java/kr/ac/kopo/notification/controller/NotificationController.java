package kr.ac.kopo.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import kr.ac.kopo.notification.service.NotificationService;

@Controller
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	/**
	 * 알림 목록
	 * GET /notification
	 */
	@GetMapping({"/notification", "/notification/"})
	public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		model.addAttribute("notifications", notificationService.getList(userDetails.getUsername()));
		return "notification/list";
	}

	/**
	 * 전체 읽음 처리
	 * POST /notification/readAll
	 */
	@PostMapping("/notification/readAll")
	public String readAll(@AuthenticationPrincipal UserDetails userDetails) {
		notificationService.readAll(userDetails.getUsername());
		return "redirect:/notification";
	}
}
