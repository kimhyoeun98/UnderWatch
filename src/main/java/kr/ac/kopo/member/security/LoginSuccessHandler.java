package kr.ac.kopo.member.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.kopo.member.service.MemberService;

/**
 * M-10 폼 로그인 성공 처리.
 * 탈퇴 유예(WITHDRAWN, 7일 이내) 상태로 로그인에 성공하면 계정을 다시 ACTIVE 로 복구한다.
 * (7일 경과분은 MemberDetailsService 에서 이미 로그인 자체가 차단된다.)
 */
@Component("loginSuccessHandler")
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Autowired
	private MemberService memberService;

	public LoginSuccessHandler() {
		setDefaultTargetUrl("/");
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		// 탈퇴 유예 상태였다면 복구(그 외 계정은 0건 업데이트로 무해)
		memberService.reactivateOnLogin(authentication.getName());
		super.onAuthenticationSuccess(request, response, authentication);
	}
}
