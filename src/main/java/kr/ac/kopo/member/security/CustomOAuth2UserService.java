package kr.ac.kopo.member.security;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import kr.ac.kopo.member.dao.MemberDAO;
import kr.ac.kopo.member.vo.MemberVO;

/**
 * M-03 소셜 로그인: OAuth2 제공자 사용자 정보를 ow_member 와 연동한다.
 * - (provider, provider_id) 로 기존 회원 조회 → 있으면 로그인
 * - 없으면 고유 ID/닉네임을 생성해 신규 가입(비밀번호 없음)
 * 반환 principal 은 폼 로그인과 동일한 {@link OwUserPrincipal} 타입이라
 * 기존 컨트롤러/JSP 코드가 그대로 동작한다.
 */
@Service("customOAuth2UserService")
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	@Autowired
	private MemberDAO memberDAO;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		String provider = userRequest.getClientRegistration().getRegistrationId(); // kakao/naver/google

		SocialInfo info = extract(provider, oAuth2User.getAttributes());
		if (info.providerId == null) {
			throw new UsernameNotFoundException("소셜 계정 식별자를 가져올 수 없습니다: " + provider);
		}

		MemberVO member = memberDAO.selectByProvider(provider, info.providerId);
		if (member == null) {
			member = register(provider, info);
		} else if ("SUSPENDED".equals(member.getStatus())) {
			throw new UsernameNotFoundException("정지된 계정입니다.");
		}

		// 소셜 계정은 password 가 null 이므로 그대로 전달(폼 로그인 검증을 타지 않음)
		return new OwUserPrincipal(
				member.getId(),
				member.getPassword(),
				member.getRole() == null ? "ROLE_USER" : member.getRole(),
				oAuth2User.getAttributes()
		);
	}

	/** 신규 소셜 회원 생성 */
	private MemberVO register(String provider, SocialInfo info) {
		MemberVO m = new MemberVO();
		m.setId(uniqueId(provider, info.providerId));
		m.setNickname(uniqueNickname(info.nickname));
		m.setName(info.name != null ? info.name : info.nickname != null ? info.nickname : "소셜회원");
		// 이메일이 없거나 이미 사용 중이면(UNIQUE 제약) null 로 저장
		m.setEmail((info.email != null && !memberDAO.existsEmail(info.email)) ? info.email : null);
		m.setProvider(provider);
		m.setProviderId(info.providerId);
		memberDAO.insertSocial(m);

		// role/status 등 DEFAULT 가 채워진 완전한 행을 다시 조회해 반환
		return memberDAO.selectById(m.getId());
	}

	/** ow_member.id (VARCHAR2(20)) 에 맞는 고유 ID 생성 */
	private String uniqueId(String provider, String providerId) {
		String prefix = provider.substring(0, 1);          // k / n / g
		String base = (prefix + providerId).replaceAll("[^a-zA-Z0-9_]", "");
		if (base.length() > 20) base = base.substring(0, 20);
		String candidate = base;
		int n = 0;
		while (memberDAO.existsId(candidate)) {
			String suffix = String.valueOf(++n);
			candidate = base.substring(0, Math.min(base.length(), 20 - suffix.length())) + suffix;
		}
		return candidate;
	}

	/** 닉네임 (VARCHAR2(50), UNIQUE) 중복 회피 */
	private String uniqueNickname(String desired) {
		String base = (desired == null || desired.isBlank()) ? "오버워치" : desired.trim();
		if (base.length() > 40) base = base.substring(0, 40);
		String candidate = base;
		while (memberDAO.existsNickname(candidate)) {
			candidate = base + ThreadLocalRandom.current().nextInt(1000, 10000);
		}
		return candidate;
	}

	// ---- 제공자별 응답 정규화 ----
	private SocialInfo extract(String provider, Map<String, Object> attr) {
		return switch (provider) {
			case "kakao"  -> kakao(attr);
			case "naver"  -> naver(attr);
			case "google" -> google(attr);
			default       -> new SocialInfo(null, null, null, null);
		};
	}

	@SuppressWarnings("unchecked")
	private SocialInfo kakao(Map<String, Object> attr) {
		String providerId = String.valueOf(attr.get("id"));
		Map<String, Object> account = (Map<String, Object>) attr.get("kakao_account");
		Map<String, Object> profile = account != null ? (Map<String, Object>) account.get("profile") : null;
		String nickname = profile != null ? (String) profile.get("nickname") : null;
		String email = account != null ? (String) account.get("email") : null;
		return new SocialInfo(providerId, nickname, nickname, email);
	}

	@SuppressWarnings("unchecked")
	private SocialInfo naver(Map<String, Object> attr) {
		Map<String, Object> resp = (Map<String, Object>) attr.get("response");
		if (resp == null) return new SocialInfo(null, null, null, null);
		return new SocialInfo(
				(String) resp.get("id"),
				(String) resp.get("nickname"),
				(String) resp.get("name"),
				(String) resp.get("email"));
	}

	private SocialInfo google(Map<String, Object> attr) {
		return new SocialInfo(
				(String) attr.get("sub"),
				(String) attr.get("name"),
				(String) attr.get("name"),
				(String) attr.get("email"));
	}

	/** 정규화된 소셜 사용자 정보 */
	private record SocialInfo(String providerId, String nickname, String name, String email) {}
}
