package kr.ac.kopo.member.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * 폼 로그인과 소셜 로그인이 공유하는 인증 주체.
 * UserDetails(폼/기존 컨트롤러용)와 OAuth2User(소셜 로그인용)를 모두 구현해,
 * 기존 {@code @AuthenticationPrincipal UserDetails} 및
 * JSP의 {@code userPrincipal.name}(= ow_member.id) 코드가 그대로 동작한다.
 */
public class OwUserPrincipal implements UserDetails, OAuth2User {

	private final String id;
	private final String password;                  // 소셜 계정은 null 가능
	private final String role;                      // 예: ROLE_USER / ROLE_ADMIN
	private final Map<String, Object> attributes;   // 소셜 원본 속성(폼 로그인은 빈 맵)

	public OwUserPrincipal(String id, String password, String role, Map<String, Object> attributes) {
		this.id = id;
		this.password = password;
		this.role = role;
		this.attributes = attributes == null ? Map.of() : attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role));
	}

	// --- UserDetails ---
	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return id;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	// --- OAuth2User ---
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getName() {
		return id;   // principal 이름 = 회원 ID
	}

	public String getRole() {
		return role;
	}
}
