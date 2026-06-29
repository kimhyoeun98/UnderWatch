package kr.ac.kopo.message.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.ac.kopo.member.service.MemberService;
import kr.ac.kopo.message.service.MessageService;
import kr.ac.kopo.message.vo.MessageVO;

@Controller
public class MessageController {

	@Autowired
	private MessageService messageService;

	@Autowired
	private MemberService memberService;

	/**
	 * 대화 목록
	 * GET /message
	 */
	@GetMapping({"/message", "/message/"})
	public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		String id = userDetails.getUsername();
		model.addAttribute("conversations", messageService.getConversations(id));
		return "message/list";
	}

	/**
	 * 채팅 스레드
	 * GET /message/chat?with=userId
	 */
	@GetMapping("/message/chat")
	public String chat(@AuthenticationPrincipal UserDetails userDetails,
					   @RequestParam("with") String partnerId,
					   Model model) {
		String myId = userDetails.getUsername();
		messageService.readAll(myId, partnerId);
		List<MessageVO> msgs = messageService.getThread(myId, partnerId);
		String partnerNick = partnerId;
		for (MessageVO m : msgs) {
			if (partnerId.equals(m.getSenderId())) { partnerNick = m.getSenderNickname(); break; }
		}
		model.addAttribute("messages", msgs);
		model.addAttribute("partnerId", partnerId);
		model.addAttribute("partnerNickname", partnerNick);
		model.addAttribute("myId", myId);
		return "message/chat";
	}

	/**
	 * 쪽지 보내기 폼 (to: 받는 아이디 미리 채움)
	 * GET /message/write
	 */
	@GetMapping("/message/write")
	public String writeForm(@RequestParam(value = "to", required = false) String to, Model model) {
		model.addAttribute("to", to);
		return "message/write";
	}

	/**
	 * 쪽지 보내기 처리
	 * POST /message/write
	 */
	@PostMapping("/message/write")
	public String write(@AuthenticationPrincipal UserDetails userDetails,
						@RequestParam("receiverId") String receiverId,
						@RequestParam("content") String content,
						@RequestParam(value = "redirectTo", required = false) String redirectTo,
						RedirectAttributes ra) {
		if (memberService.findById(receiverId) == null) {
			ra.addFlashAttribute("error", "받는 사람 아이디가 존재하지 않습니다.");
			return "redirect:/message/write";
		}
		messageService.send(userDetails.getUsername(), receiverId, content);
		if ("chat".equals(redirectTo)) {
			return "redirect:/message/chat?with=" + receiverId;
		}
		ra.addFlashAttribute("msg", "쪽지를 보냈습니다.");
		return "redirect:/message";
	}

	/**
	 * 읽음 처리
	 * POST /message/read
	 */
	@PostMapping("/message/read")
	public String read(@AuthenticationPrincipal UserDetails userDetails,
					   @RequestParam("no") int no) {
		messageService.read(no, userDetails.getUsername());
		return "redirect:/message";
	}

	/**
	 * 나에게만 삭제
	 * POST /message/delete
	 */
	@PostMapping("/message/delete")
	public String delete(@AuthenticationPrincipal UserDetails userDetails,
						 @RequestParam("no") int no,
						 @RequestParam("partnerId") String partnerId) {
		messageService.deleteForMe(no, userDetails.getUsername());
		return "redirect:/message/chat?with=" + partnerId;
	}

	/**
	 * 대화방 나가기 (나에게만 전체 삭제)
	 * POST /message/leave
	 */
	@PostMapping("/message/leave")
	public String leave(@AuthenticationPrincipal UserDetails userDetails,
						@RequestParam("partnerId") String partnerId) {
		messageService.leaveConversation(userDetails.getUsername(), partnerId);
		return "redirect:/message";
	}
}
