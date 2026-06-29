package kr.ac.kopo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * M-03 소셜 로그인 ClientRegistration 설정.
 * 카카오/네이버/구글 모두 수동 등록하며, 구글도 openid 스코프를 빼서 OIDC가 아닌
 * 일반 OAuth2 흐름으로 두어 세 제공자를 동일한 CustomOAuth2UserService 로 처리한다.
 * client-id 가 비어 있는 제공자는 등록을 건너뛰어 앱이 정상 기동되도록 한다
 * (예: 카카오 키만 발급받은 상태).
 */
@Configuration
public class OAuthConfig {

	@Value("${oauth.kakao.client-id:}")
	private String kakaoClientId;
	@Value("${oauth.kakao.client-secret:}")
	private String kakaoClientSecret;

	@Value("${oauth.naver.client-id:}")
	private String naverClientId;
	@Value("${oauth.naver.client-secret:}")
	private String naverClientSecret;

	@Value("${oauth.google.client-id:}")
	private String googleClientId;
	@Value("${oauth.google.client-secret:}")
	private String googleClientSecret;

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		List<ClientRegistration> registrations = new ArrayList<>();
		if (hasText(kakaoClientId))  registrations.add(kakao());
		if (hasText(naverClientId))  registrations.add(naver());
		if (hasText(googleClientId)) registrations.add(google());

		if (registrations.isEmpty()) {
			// 키가 하나도 없으면 InMemory 레포지토리가 빈 리스트를 거부하므로,
			// 비활성 더미를 넣어 기동만 시킨다(버튼은 동작하지 않음).
			registrations.add(disabledPlaceholder());
		}
		return new InMemoryClientRegistrationRepository(registrations);
	}

	private ClientRegistration kakao() {
		return ClientRegistration.withRegistrationId("kakao")
				.clientId(kakaoClientId)
				.clientSecret(hasText(kakaoClientSecret) ? kakaoClientSecret : null)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
				.scope("profile_nickname", "account_email")
				.authorizationUri("https://kauth.kakao.com/oauth/authorize")
				.tokenUri("https://kauth.kakao.com/oauth/token")
				.userInfoUri("https://kapi.kakao.com/v2/user/me")
				.userNameAttributeName("id")
				.clientName("Kakao")
				.build();
	}

	private ClientRegistration naver() {
		return ClientRegistration.withRegistrationId("naver")
				.clientId(naverClientId)
				.clientSecret(hasText(naverClientSecret) ? naverClientSecret : null)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
				.scope("name", "email", "nickname")
				.authorizationUri("https://nid.naver.com/oauth2.0/authorize")
				.tokenUri("https://nid.naver.com/oauth2.0/token")
				.userInfoUri("https://openapi.naver.com/v1/nid/me")
				.userNameAttributeName("response")
				.clientName("Naver")
				.build();
	}

	private ClientRegistration google() {
		// openid 스코프를 빼서 OIDC 가 아닌 일반 OAuth2 흐름으로 등록한다.
		// (그래야 카카오/네이버와 동일하게 customOAuth2UserService 로 처리되어 ow_member 와 연동됨)
		return ClientRegistration.withRegistrationId("google")
				.clientId(googleClientId)
				.clientSecret(hasText(googleClientSecret) ? googleClientSecret : null)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
				.scope("profile", "email")
				.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
				.tokenUri("https://www.googleapis.com/oauth2/v4/token")
				.userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
				.userNameAttributeName("sub")
				.clientName("Google")
				.build();
	}

	/** 키가 전혀 없을 때 기동만 시키기 위한 비활성 더미(실제 호출되지 않음). */
	private ClientRegistration disabledPlaceholder() {
		return ClientRegistration.withRegistrationId("disabled")
				.clientId("disabled")
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
				.authorizationUri("https://example.invalid/authorize")
				.tokenUri("https://example.invalid/token")
				.userInfoUri("https://example.invalid/me")
				.userNameAttributeName("id")
				.clientName("disabled")
				.build();
	}

	private static boolean hasText(String s) {
		return s != null && !s.trim().isEmpty();
	}
}
