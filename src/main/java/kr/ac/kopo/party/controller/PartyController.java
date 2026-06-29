package kr.ac.kopo.party.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.ac.kopo.party.service.PartyService;
import kr.ac.kopo.party.vo.PartyVO;

@Controller
public class PartyController {

	@Autowired
	private PartyService partyService;

	/**
	 * P-02 목록 + 동적 필터
	 * GET /party/list
	 */
	@GetMapping("/party/list")
	public String list(@ModelAttribute PartyVO filter, Model model) {
		model.addAttribute("parties", partyService.getList(filter));
		model.addAttribute("filter", filter);
		return "party/list";
	}

	/**
	 * P-01 모집글 작성폼
	 * GET /party/write
	 */
	@GetMapping("/party/write")
	public String writeForm(Model model) {
		model.addAttribute("partyVO", new PartyVO());
		return "party/write";
	}

	/**
	 * P-01 모집글 작성처리
	 * POST /party/write
	 */
	@PostMapping("/party/write")
	public String write(@AuthenticationPrincipal UserDetails userDetails,
						@ModelAttribute PartyVO party) {
		party.setWriterId(userDetails.getUsername());
		if (party.getMicYn() == null) party.setMicYn("N");
		partyService.write(party);
		return "redirect:/party/list";
	}

	/**
	 * P-03 모집 상태 토글
	 * POST /party/status
	 */
	@PostMapping("/party/status")
	public String toggle(@AuthenticationPrincipal UserDetails userDetails,
						 @RequestParam("no") int no) {
		partyService.toggleStatus(no, userDetails.getUsername());
		return "redirect:/party/list";
	}
}
