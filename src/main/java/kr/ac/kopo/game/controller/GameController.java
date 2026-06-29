package kr.ac.kopo.game.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import kr.ac.kopo.game.service.GameService;

@Controller
public class GameController {

	@Autowired
	private GameService gameService;

	/**
	 * G-01 영웅 목록 — 역할별로 분류해서 전달
	 * GET /game/heroes
	 */
	@GetMapping("/game/heroes")
	public String heroes(Model model) {
		model.addAttribute("tanks", gameService.getHeroes("tank"));
		model.addAttribute("damages", gameService.getHeroes("damage"));
		model.addAttribute("supports", gameService.getHeroes("support"));
		return "game/heroes";
	}

	/**
	 * G-01 영웅 상세
	 * GET /game/heroes/{key}
	 */
	@GetMapping("/game/heroes/{key}")
	public String hero(@PathVariable("key") String key, Model model) {
		model.addAttribute("hero", gameService.getHero(key));
		model.addAttribute("heroKey", key);
		return "game/hero";
	}

	/**
	 * G-02 맵 목록
	 * GET /game/maps
	 */
	@GetMapping("/game/maps")
	public String maps(Model model) {
		model.addAttribute("maps", gameService.getMaps());
		return "game/maps";
	}

	/**
	 * G-03 패치 노트
	 * GET /game/patches
	 */
	@GetMapping("/game/patches")
	public String patches(Model model) {
		model.addAttribute("patches", gameService.getPatches());
		return "game/patches";
	}
}
