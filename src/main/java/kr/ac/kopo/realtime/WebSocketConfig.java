package kr.ac.kopo.realtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 실시간 알림용 WebSocket 엔드포인트 등록.
 * DispatcherServlet 컨텍스트에서 컴포넌트 스캔되어 핸드셰이크 매핑이 활성화된다.
 *
 * 엔드포인트: ws(s)://host/{contextPath}/ws/notify
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Autowired
	private NotifyWebSocketHandler notifyHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(notifyHandler, "/ws/notify")
				.addInterceptors(new UserHandshakeInterceptor())
				.setAllowedOriginPatterns("*");
	}
}
