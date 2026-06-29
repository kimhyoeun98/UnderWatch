package kr.ac.kopo.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kr.ac.kopo.board.service.BoardService;
import kr.ac.kopo.board.vo.SearchVO;
import kr.ac.kopo.party.service.PartyService;
import kr.ac.kopo.party.vo.PartyVO;

@Controller
public class MainController {

	@Autowired
	private BoardService boardService;

	@Autowired
	private PartyService partyService;

	/**
	 * 메인 페이지
	 * GET /
	 */
	@GetMapping("/")
	public String index(Model model) {
		// 메인 페이지: 최근 게시글 목록을 바로 노출 (디시형 구조)
		SearchVO search = new SearchVO();
		search.setPerPage(20);
		boardService.search(search);
		model.addAttribute("recentPosts", boardService.getList(search));
		model.addAttribute("categories", boardService.getCategories());

		// 사이드: 모집 중인 구인구직
		PartyVO partyFilter = new PartyVO();
		partyFilter.setStatus("RECRUITING");
		model.addAttribute("recentParties", partyService.getList(partyFilter));
		return "index";
	}

	/**
	 * 접근 거부 페이지
	 * GET /error/403
	 */
	@GetMapping("/error/403")
	public String error403() {
		return "error/403";
	}
}
