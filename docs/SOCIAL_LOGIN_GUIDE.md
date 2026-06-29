# UnderWatch 소셜 로그인(M-03) 설정 가이드 

카카오 / 네이버 / 구글 OAuth2 로그인을 붙이는 방법입니다.
(Spring Security 6.4 OAuth2 Client, 순수 XML MVC 기준)

> 실제 동작에는 각 제공자에서 발급한 **client id / client secret** 이 필요합니다.

---

## 1. 의존성 추가 (pom.xml)

```xml
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-oauth2-client</artifactId>
  <version>6.4.5</version>
</dependency>
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-oauth2-jose</artifactId>
  <version>6.4.5</version>
</dependency>
```

## 2. OAuth 앱 등록 & 키 발급

각 개발자 콘솔에서 앱을 만들고 **Redirect URI**를 등록합니다(콘솔 등록값과 코드가 정확히 일치해야 함):

| 제공자 | 콘솔 | Redirect URI |
|--------|------|--------------|
| 구글 | console.cloud.google.com → API/서비스 → 사용자 인증 정보 → OAuth 클라이언트 ID | `http://localhost:8080/UnderWatch/login/oauth2/code/google` |
| 카카오 | developers.kakao.com → 내 애플리케이션 → 카카오 로그인 | `http://localhost:8080/UnderWatch/login/oauth2/code/kakao` |
| 네이버 | developers.naver.com → 애플리케이션 등록 | `http://localhost:8080/UnderWatch/login/oauth2/code/naver` |

발급된 **client id / secret** 을 메모해 둡니다. (카카오는 REST API 키 = client id, "보안 → Client Secret" 발급)

## 3. ClientRegistration 빈 (config/spring/spring-oauth.xml 새로 만들어 import)

구글은 Spring이 기본 제공(CommonOAuth2Provider), 카카오/네이버는 수동 등록이 필요합니다.

```xml
<bean id="clientRegistrationRepository"
      class="org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository">
  <constructor-arg>
    <list>
      <!-- 구글 -->
      <bean class="org.springframework.security.oauth2.client.registration.ClientRegistration" factory-method="..."/>
      <!-- 실무에선 ClientRegistrations.fromIssuerLocation / CommonOAuth2Provider.GOOGLE.getBuilder("google") 로 생성 -->
    </list>
  </constructor-arg>
</bean>
```

> XML 한 줄로 ClientRegistration을 만들기 번거로우므로, **이 부분만 @Configuration 자바 클래스**로 두는 것을 권장합니다:

```java
@Configuration
public class OAuthConfig {
  @Bean
  public ClientRegistrationRepository clientRegistrationRepository() {
    return new InMemoryClientRegistrationRepository(google(), kakao(), naver());
  }
  private ClientRegistration google() {
    return CommonOAuth2Provider.GOOGLE.getBuilder("google")
        .clientId("발급-구글-ID").clientSecret("발급-구글-SECRET").build();
  }
  private ClientRegistration kakao() {
    return ClientRegistration.withRegistrationId("kakao")
        .clientId("발급-카카오-REST-KEY").clientSecret("발급-카카오-SECRET")
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
        .scope("profile_nickname", "account_email")
        .authorizationUri("https://kauth.kakao.com/oauth/authorize")
        .tokenUri("https://kauth.kakao.com/oauth/token")
        .userInfoUri("https://kapi.kakao.com/v2/user/me")
        .userNameAttributeName("id")
        .clientName("Kakao").build();
  }
  private ClientRegistration naver() {
    return ClientRegistration.withRegistrationId("naver")
        .clientId("발급-네이버-ID").clientSecret("발급-네이버-SECRET")
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
        .scope("name", "email", "nickname")
        .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
        .tokenUri("https://nid.naver.com/oauth2.0/token")
        .userInfoUri("https://openapi.naver.com/v1/nid/me")
        .userNameAttributeName("response")
        .clientName("Naver").build();
  }
}
```

## 4. spring-security.xml 에 oauth2-login 추가

```xml
<security:http>
  ... 기존 설정 ...
  <security:oauth2-login login-page="/member/login"
                         user-service-ref="customOAuth2UserService"/>
</security:http>
```

## 5. 회원 연동 (CustomOAuth2UserService)

`DefaultOAuth2UserService`를 확장해 소셜 사용자 정보를 `ow_member`와 연동합니다.

- 소셜 이메일/고유 id로 기존 회원 조회 → 있으면 로그인, 없으면 신규 가입(임시 닉네임)
- 최초 로그인 시 닉네임/추가정보 입력 페이지로 유도(SRS 요구)
- 권장: `ow_member`에 `provider`(google/kakao/naver), `provider_id` 컬럼을 추가해 일반가입과 구분/중복 방지

```sql
ALTER TABLE ow_member ADD (provider VARCHAR2(20), provider_id VARCHAR2(100));
```

## 6. 주의사항

- **Redirect URI**는 콘솔 등록값과 코드(`{baseUrl}/login/oauth2/code/{registrationId}`)가 정확히 일치해야 함. 컨텍스트 루트(`/UnderWatch`) 포함.
- 카카오/네이버는 `CommonOAuth2Provider`에 없어 위처럼 **수동 ClientRegistration** 필요.
- 비밀번호가 없는 소셜 계정이므로 `ow_member.password`를 NOT NULL로 두면 가입 시 임의 값/난수를 넣거나 컬럼을 nullable로 변경.
- 운영 환경에서는 https + 실제 도메인으로 Redirect URI 재등록.
