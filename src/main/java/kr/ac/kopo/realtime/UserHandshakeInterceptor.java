package kr.ac.kopo.realtime;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket 핸드셰이크 시점에 인증된 사용자 아이디를 세션 속성("userId")에 저장한다.
 * 핸드셰이크 요청은 Spring Security 필터 체인을 거치므로 이 시점에 사용자 정보가 들어 있다.
 * 비로그인 연결은 거부한다.
 */
public class UserHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
								   WebSocketHandler wsHandler, Map<String, Object> attributes) {
		Principal principal = request.getPrincipal();
		if (principal == null) {
			return false; // 인증되지 않은 연결은 거부
		}
		attributes.put("userId", principal.getName());
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
							   WebSocketHandler wsHandler, Exception exception) {
		// 별도 처리 없음
	}
}
