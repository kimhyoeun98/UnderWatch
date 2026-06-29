package kr.ac.kopo.member.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 소셜 로그인 실패 원인을 콘솔에 출력한 뒤 /member/login?error 로 보낸다.
 * (기본 핸들러는 원인을 숨겨 디버깅이 어려우므로 진단용으로 둔다.)
 */
public class OAuthLoggingFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	public OAuthLoggingFailureHandler() {
		super("/member/login?error=true");
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
										AuthenticationException exception)
			throws IOException, ServletException {
		System.err.println("===== [OAuth 로그인 실패] =====");
		System.err.println("message: " + exception.getMessage());
		if (exception instanceof OAuth2AuthenticationException oae) {
			System.err.println("error  : " + oae.getError());   // code + description + uri
		}
		Throwable cause = exception.getCause();
		if (cause != null) {
			System.err.println("cause  : " + cause);
		}
		exception.printStackTrace();
		System.err.println("==============================");
		super.onAuthenticationFailure(request, response, exception);
	}
}
