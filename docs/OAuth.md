# OAuth2 소셜 로그인 

카카오, 네이버, 구글 OAuth2 로그인 구현 문서입니다.

---

## 1. 구성 요소

| 파일                                | 역할                                |
| --------------------------------- | --------------------------------- |
| `pom.xml`                         | OAuth2 Client 의존성                 |
| `oauth.properties`                | 제공자별 client-id / client-secret    |
| `OAuthConfig.java`                | ClientRegistrationRepository 빈 등록 |
| `CustomOAuth2UserService.java`    | 소셜 사용자와 회원 테이블 연동                 |
| `OwUserPrincipal.java`            | 폼 / 소셜 / 얼굴 공용 Principal          |
| `OAuthLoggingFailureHandler.java` | OAuth 실패 원인 로깅 및 리다이렉트            |
| `spring-security.xml`             | OAuth2 로그인 설정                     |

---

## 2. 키 외부화

```properties
oauth.kakao.client-id=
oauth.kakao.client-secret=
oauth.naver.client-id=
oauth.naver.client-secret=
oauth.google.client-id=
oauth.google.client-secret=
```

* 키가 비어 있는 제공자는 자동 비활성화
* 활성화된 제공자만 로그인 버튼 표시
* 로컬 개발 시 `oauth.properties`는 외부 설정 파일로 관리

---

## 3. ClientRegistration

카카오와 네이버는 Spring Security의 기본 제공 Provider가 아니므로 수동 등록했습니다.
구글은 `openid` 스코프를 제외하여 OIDC가 아닌 일반 OAuth2 흐름으로 통일했습니다.

```java
ClientRegistration.withRegistrationId("kakao")
    .clientId(kakaoClientId)
    .clientSecret(kakaoClientSecret)
    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
    .scope("profile_nickname", "account_email")
    .authorizationUri("https://kauth.kakao.com/oauth/authorize")
    .tokenUri("https://kauth.kakao.com/oauth/token")
    .userInfoUri("https://kapi.kakao.com/v2/user/me")
    .userNameAttributeName("id")
    .build();
```

---

## 4. 사용자 연동 흐름

```text
OAuth 로그인 요청
      ↓
제공자 인증
      ↓
사용자 정보 조회
      ↓
provider + provider_id로 기존 회원 조회
      ↓
없으면 자동 회원가입
      ↓
OwUserPrincipal 반환
      ↓
SecurityContext 저장
```

자동 가입 시 처리 내용은 다음과 같습니다.

* 고유 회원 ID 생성
* 닉네임 중복 회피
* 이메일 중복 시 null 저장
* 소셜 회원은 password null 허용
* provider, provider_id 저장

---

## 5. 공용 Principal

`OwUserPrincipal`은 `UserDetails`와 `OAuth2User`를 동시에 구현합니다.
이를 통해 기존 코드가 로그인 방식에 의존하지 않도록 했습니다.

```text
Form Login  ─┐
OAuth Login ─┼─▶ OwUserPrincipal
Face Login  ─┘
```

---

## 6. Security 설정

```xml
<security:oauth2-login
    login-page="/member/login"
    authentication-failure-handler-ref="oauthFailureHandler"
    client-registration-repository-ref="clientRegistrationRepository"
    user-service-ref="customOAuth2UserService" />
```

OAuth2 로그인 실패 처리는 `authentication-failure-url` 속성이 아닌 커스텀 실패 핸들러로 처리했습니다.

---

## 7. 카카오 설정 예시

| 항목           | 값                                               |
| ------------ | ----------------------------------------------- |
| Redirect URI | `http://localhost:8080/login/oauth2/code/kakao` |
| 사이트 도메인      | `http://localhost:8080`                         |
| 동의항목         | 닉네임, 이메일                                        |
| 호출 허용 IP     | 로컬 개발 시 비워둠                                     |

---

## 8. 트러블슈팅

| 증상                      | 원인               | 해결                                  |
| ----------------------- | ---------------- | ----------------------------------- |
| `/login?error` 404      | OAuth 실패 URL 미지정 | 커스텀 실패 핸들러 적용                       |
| `401 ip mismatched`     | 카카오 호출 허용 IP 제한  | 콘솔에서 IP 제한 해제                       |
| `redirect_uri mismatch` | 등록 URI 불일치       | localhost / underwatch.local URI 등록 |
| 구글 연동 로직 미호출            | OIDC 흐름 진입       | `openid` 스코프 제외                     |
