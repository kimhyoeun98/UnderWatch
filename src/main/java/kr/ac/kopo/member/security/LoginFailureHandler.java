package kr.ac.kopo.member.security;

import java.io.IOException;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 폼 로그인 실패 처리.
 * 정지(SUSPENDED) 계정은 LockedException 으로 구분되어 ?error=suspended 로,
 * 그 외(아이디/비밀번호 불일치 등)는 ?error=true 로 로그인 페이지에 안내한다.
 */
@Component("loginFailureHandler")
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		// DefaultRedirectStrategy 가 contextPath 를 자동으로 붙이므로 여기서는 붙이지 않는다.
		String target = (exception instanceof LockedException)
				? "/member/login?error=suspended"
				: "/member/login?error=true";
		getRedirectStrategy().sendRedirect(request, response, target);
	}
}
