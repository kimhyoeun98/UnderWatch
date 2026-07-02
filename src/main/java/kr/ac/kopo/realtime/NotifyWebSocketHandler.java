package kr.ac.kopo.realtime;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 사용자별 WebSocket 세션을 관리하고, 안 읽은 쪽지/알림 개수를
 * 실시간으로 푸시한다. (raw WebSocket — STOMP 미사용)
 *
 * 서비스 계층(MessageServiceImpl, NotificationServiceImpl)이 이 핸들러를 주입받아
 * 쪽지/알림 발생 직후 {@link #sendBadge(String, String, int)} 를 호출한다.
 */
@Component
public class NotifyWebSocketHandler extends TextWebSocketHandler {

	/** userId -> 그 사용자의 열린 세션들(여러 탭 동시 접속 가능) */
	private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		String userId = userId(session);
		if (userId == null) {
			return;
		}
		sessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		String userId = userId(session);
		if (userId == null) {
			return;
		}
		Set<WebSocketSession> set = sessions.get(userId);
		if (set != null) {
			set.remove(session);
			if (set.isEmpty()) {
				sessions.remove(userId);
			}
		}
	}

	/**
	 * 특정 사용자의 모든 열린 탭에 배지 갱신 신호를 보낸다.
	 *
	 * @param userId 받는 사람 아이디
	 * @param type   "message" 또는 "notification"
	 * @param count  현재 안 읽은 개수
	 */
	public void sendBadge(String userId, String type, int count) {
		Set<WebSocketSession> set = sessions.get(userId);
		if (set == null || set.isEmpty()) {
			return; // 받는 사람이 접속 중이 아니면 보낼 필요 없음 (다음 새로고침 때 반영)
		}
		TextMessage msg = new TextMessage("{\"type\":\"" + type + "\",\"count\":" + count + "}");
		for (WebSocketSession s : set) {
			try {
				if (s.isOpen()) {
					// WebSocketSession 은 동시 전송에 안전하지 않으므로 세션 단위로 동기화
					synchronized (s) {
						s.sendMessage(msg);
					}
				}
			} catch (IOException ignore) {
				// 전송 실패한 세션은 곧 afterConnectionClosed 에서 정리됨
			}
		}
	}

	private String userId(WebSocketSession session) {
		Object v = session.getAttributes().get("userId");
		return v == null ? null : v.toString();
	}
}
