package kr.ac.kopo.main;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import kr.ac.kopo.board.service.BoardService;
import kr.ac.kopo.board.vo.BoardCategoryVO;
import kr.ac.kopo.message.service.MessageService;
import kr.ac.kopo.notification.service.NotificationService;

/**
 * 모든 화면(헤더의 카테고리 네비바 등)에서 카테고리 목록을 쓸 수 있도록
 * 전역 모델 속성으로 주입한다.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

	@Autowired
	private BoardService boardService;

	@Autowired
	private MessageService messageService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;

	@ModelAttribute("categories")
	public List<BoardCategoryVO> categories() {
		return boardService.getCategories();
	}

	/**
	 * M-03 실제 키가 등록된 소셜 제공자 id 목록(kakao/naver/google).
	 * JSP에서 키 없는 버튼을 숨기는 데 사용한다.
	 */
	@ModelAttribute("oauthProviders")
	public Set<String> oauthProviders() {
		Set<String> ids = new LinkedHashSet<>();
		if (clientRegistrationRepository instanceof InMemoryClientRegistrationRepository repo) {
			for (ClientRegistration reg : repo) {
				if (!"disabled".equals(reg.getRegistrationId())) {
					ids.add(reg.getRegistrationId());
				}
			}
		}
		return ids;
	}

	@ModelAttribute("unreadMessages")
	public int unreadMessages(@AuthenticationPrincipal UserDetails user) {
		return user == null ? 0 : messageService.countUnread(user.getUsername());
	}

	@ModelAttribute("unreadNotifications")
	public int unreadNotifications(@AuthenticationPrincipal UserDetails user) {
		return user == null ? 0 : notificationService.countUnread(user.getUsername());
	}
}
