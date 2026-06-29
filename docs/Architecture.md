# Architecture 

UnderWatch의 전체 구조와 요청 처리 흐름을 정리한 문서입니다.

---

## 1. 한눈에 보기

```text
Browser (JSP + Bootstrap)
      │  HTTP
      ▼
[ Security Filter Chain ]  ← DelegatingFilterProxy
      │
      ▼
DispatcherServlet (spring-mvc.xml)
      │
      ▼
Controller ──▶ Service ──▶ DAO ──▶ MyBatis Mapper(XML) ──▶ Oracle XE
      │
      ▼
View Resolver ▶ JSP (/WEB-INF/jsp/**)
```

* **언어/런타임**: Java 17
* **프레임워크**: Spring MVC 6.2, Spring Security 6.4
* **설정 방식**: XML 중심 설정 + OAuth ClientRegistration Java Config
* **영속성**: MyBatis 3.5 + Oracle XE, Commons DBCP
* **뷰**: JSP + JSTL, Bootstrap 5, 자체 테마 CSS
* **서버**: Apache Tomcat 11

---

## 2. 단일 컨텍스트 구조

UnderWatch는 루트 ApplicationContext 없이 DispatcherServlet 컨텍스트 하나만 사용합니다.

* `web.xml`에서 DispatcherServlet이 `spring-mvc.xml` 로드
* Spring Security 필터는 `DelegatingFilterProxy`로 연결
* `spring-mvc.xml`에서 `spring-security.xml` import
* MVC 빈과 Security 빈이 같은 컨텍스트에 존재

```xml
<filter>
  <filter-name>springSecurityFilterChain</filter-name>
  <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
  <init-param>
    <param-name>contextAttribute</param-name>
    <param-value>org.springframework.web.servlet.FrameworkServlet.CONTEXT.dispatcher</param-value>
  </init-param>
</filter>
```

---

## 3. 주요 설정 파일

| 파일                           | 역할                                                                |
| ---------------------------- | ----------------------------------------------------------------- |
| `web.xml`                    | DispatcherServlet, Security Filter, Encoding Filter, Multipart 설정 |
| `spring-mvc.xml`             | MVC 설정, 컴포넌트 스캔, DataSource, MyBatis, 인터셉터                        |
| `spring-security.xml`        | URL 접근 제어, 로그인, 로그아웃, OAuth2 설정                                   |
| `sqlMapConfig.xml`           | MyBatis 공통 설정                                                     |
| `config/sqlMap/oracle/*.xml` | 도메인별 Mapper XML                                                   |
| `oauth.properties`           | 소셜 로그인 키 외부화                                                      |
| `OAuthConfig.java`           | OAuth ClientRegistrationRepository 빈 등록                           |

---

## 4. 패키지 구조

```text
kr.ac.kopo
 ├── member
 │    ├── controller
 │    ├── service
 │    ├── dao
 │    ├── vo
 │    └── security
 ├── board
 ├── comment
 ├── report
 ├── party
 ├── message
 ├── notification
 ├── visit
 ├── game
 ├── admin
 ├── config
 └── main
```

각 도메인은 다음 4계층 구조를 따릅니다.

```text
Controller → Service → DAO → MyBatis Mapper XML → Oracle XE
```

---

## 5. 공통 처리

* `GlobalControllerAdvice`
  * 공통 카테고리 목록
  * 안 읽은 쪽지 수
  * 안 읽은 알림 수
  * 활성화된 OAuth 제공자 목록
* `VisitInterceptor`
  * 일일 방문자 집계
* `CharacterEncodingFilter`
  * UTF-8 인코딩 강제
* `HiddenHttpMethodFilter`
  * PUT / DELETE 요청 지원
* 에러 페이지
  * 403 / 404 전용 JSP 제공

---

## 6. 인증 흐름

```text
Form Login   → MemberDetailsService ─┐
OAuth2 Login → CustomOAuth2UserService ┼─▶ OwUserPrincipal ─▶ SecurityContext
Face Login   → matchFace(1:N) + 수동세션 ┘
```

세 가지 로그인 방식은 모두 `OwUserPrincipal`을 사용합니다.
따라서 Controller와 JSP는 로그인 방식과 관계없이 동일하게 동작합니다.

---

## 7. 외부 연동

| 대상             | 용도         | 비고                       |
| -------------- | ---------- | ------------------------ |
| OverFast API   | 오버워치 영웅 정보 | 키 불필요, `locale=ko-kr` 지원 |
| 카카오 / 네이버 / 구글 | OAuth2 로그인 | `oauth.properties`로 키 관리 |
