# UnderWatch 🎮
### 오버워치 커뮤니티 웹 서비스

> **Spring MVC + Spring Security + MyBatis 기반의 오버워치 커뮤니티 웹 서비스**
>
> 게시판, 구인구직, 쪽지, 알림, 게임 정보와 함께 **폼 로그인 · OAuth2 로그인 · 얼굴 인식 로그인**을 하나의 인증 구조로 통합한 프로젝트입니다.

---

# 📌 프로젝트 소개

**UnderWatch**는 오버워치 플레이어들이 정보를 공유하고 소통할 수 있는 커뮤니티 웹 서비스입니다.
단순한 게시판 CRUD 구현이 아닌, 다양한 인증 방식과 커뮤니티 기능을 하나의 시스템으로 통합하는 것을 목표로 개발했습니다.

## 프로젝트 목표

* Spring MVC 기반 커뮤니티 서비스 구현
* Spring Security를 이용한 인증 및 권한 관리
* 폼 / OAuth2 / 얼굴 인식 로그인 통합
* OverFast API · Blizzard 패치노트를 활용한 게임 정보(영웅 · 맵 · 패치) 제공
* 관리자 기능 및 운영 기능 구현

---

# 📷 프로젝트 화면

> *(스크린샷 추가 예정)*

* 메인 화면
* 로그인
* 게시판
* 게임 정보
* 관리자 페이지
* 얼굴 로그인

---

# ✨ 주요 기능

## 회원

* 회원가입
* 로그인 / 로그아웃
* 아이디 / 닉네임 / 이메일 중복 확인
* 마이페이지
* 프로필 이미지 업로드
* 활동 점수 및 등급 시스템

---

## 인증

### ✔ Form Login

Spring Security 기본 로그인

### ✔ OAuth2 Login

* 카카오
* 네이버
* 구글

최초 로그인 시 자동 회원가입을 지원합니다.

### ✔ Face Login

* LBPH 알고리즘 직접 구현 (외부 라이브러리/모델 없음)
* 얼굴 특징(히스토그램) 등록
* 1:N 얼굴 비교
* 아이디 입력 없이 로그인

---

## 게시판

* 게시글 CRUD
* 카테고리
* 검색
* 추천 / 비추천
* 이미지 업로드
* 댓글 / 대댓글
* 신고 기능

---

## 커뮤니티

* 구인구직
* 쪽지
* 알림

---

## 게임 정보

OverFast API + Blizzard 패치노트 연동

* 영웅 목록 · 역할별 필터 · 스킬 · 체력 · 배경 이야기
* 맵 목록 (맵 이름 · 게임 모드 한글화)
* 패치 노트 (공식 패치노트 목록 — 제목 · 날짜 · 원문 링크 · 썸네일)

---

## 관리자

* 회원 관리
* 신고 관리
* 카테고리 관리
* 방문 통계
* 대시보드

---

# ⭐ 핵심 구현

## 1. 하나의 Principal로 세 가지 로그인 통합

```
Form Login
            \
OAuth Login ----> OwUserPrincipal
            /
Face Login
```

세 가지 로그인 모두 동일한 Principal을 사용하도록 설계하여 Controller와 JSP를 수정하지 않고 인증 방식을 확장할 수 있도록 구현했습니다.

---

## 2. 얼굴 인식 로그인

```
웹캠 (브라우저에서 이미지 캡처)
↓
Server — LBPH 특징 추출 (직접 구현)
↓
LBPH 히스토그램
↓
1:N 비교 (카이제곱 거리)
↓
로그인
```

브라우저는 얼굴 이미지만 전송하고, 특징 추출·비교는 모두 서버에서 처리합니다.
등록된 모든 얼굴과 비교하여 가장 가까운 사용자를 찾는 방식으로 로그인합니다.

---

## 3. 게임 정보 API

오버워치 **영웅·맵** 정보는 OverFast API로, **패치 노트**는 Blizzard 공식 패치노트 페이지에서 가져와 제공합니다.

* 영웅: 목록(역할별) · 상세 — 한국어(`locale=ko-kr`)
* 맵: 목록 — 맵 이름 · 게임 모드 한글 보강
* 패치 노트: 공식 페이지를 파싱해 제목 · 날짜 · **원문 링크** · 썸네일 제공

---

# 🧩 개발하며 겪은 문제와 해결

기능을 "되게" 만드는 것보다, **왜 안 되는지**를 찾는 데 더 많은 시간을 썼습니다.
가장 기억에 남는 다섯 가지입니다.

## 1. Spring 6 전환의 숨은 함정

예전 자료(Spring 4~5 기준)대로 작성하면 문법은 맞는데 500이 나는 상황이 반복됐습니다.
*"문법은 맞는데 왜 500이지?"* 가 가장 자주 나온 말이었습니다.

* `CommonsMultipartResolver` 제거 → `StandardServletMultipartResolver`로 교체
* `@RequestParam` 이름 생략 시 깨짐 → 컴파일러 `-parameters` 옵션 필요
* `@AuthenticationPrincipal` 미동작 → 인자 리졸버(`AuthenticationPrincipalArgumentResolver`) 수동 등록
* JSP `sec:authorize` 동작 안 함 → 표현식 핸들러 빈 명시적 노출
* 파일 다운로드 깨짐 → `ResourceHttpMessageConverter` 관련 설정

➡ **레퍼런스의 버전 차이를 의심하는 습관**이 생겼습니다.

## 2. Oracle 한글 인코딩 — 가장 끈질겼던 적

체감상 DB 쪽이 제일 자주 터졌습니다.

* SQL\*Plus가 한글을 깨뜨려 카테고리·계정 데이터가 망가짐 (심지어 **주석의 한글**이 파싱을 깨뜨림)
* `UNISTR`로 우회 / 마이그레이션 SQL은 ASCII·엔티티 위주로 작성
* **CDB/PDB 접속 혼동** (`xe` vs `xepdb1`)
* 시퀀스 불일치로 `ORA-00001`(unique constraint)
* admin 계정 **BCrypt 해시 불일치**로 로그인 실패

➡ 데이터 자체보다 **인코딩·접속 경로·시퀀스 상태**가 문제의 근원인 경우가 많았습니다.

## 3. Eclipse / Tomcat 운영의 잔손질

코드가 아니라 **환경**에서 시간을 많이 뺏겼습니다.

* 컨텍스트 루트 이름 (`/UnderWatch` → `/`) 반영
* 서버를 두 개 만들다 난 **Bootstrap ClassNotFound**, 포트 충돌
* 수정할 때마다 **Clean → Restart → Ctrl+Shift+R** 를 거쳐야 결과가 보여 피드백 루프가 길었음

➡ "코드는 맞는데 화면이 안 바뀐다"의 절반은 **배포·캐시 문제**였습니다.

## 4. 인증 확장의 함정 (폼 → 소셜 → 얼굴)

| 문제 | 원인 | 해결 |
| --- | --- | --- |
| 로그인 확장 시 기존 코드 깨짐 | 인증 방식마다 Principal 타입이 다름 | `UserDetails`+`OAuth2User` 동시 구현한 **공용 `OwUserPrincipal`**로 통일 |
| 소셜 로그인 실패 시 404 | `oauth2-login`에 `authentication-failure-url` 속성이 없음 | **커스텀 실패 핸들러**로 원인 로깅 + `/member/login?error` 이동 |
| 카카오 로그인 마지막에 401 | REST API 키의 **호출 허용 IP 제한** | 콘솔에서 IP 제한 해제 (코드 문제 아님) |
| 얼굴 로그인 카메라 차단 | `getUserMedia`는 **보안 컨텍스트 전용** | 얼굴 기능은 `localhost`에서 동작하도록 안내·경고 |

➡ 세 가지 로그인을 하나의 Principal로 통합한 덕분에, Controller·JSP를 고치지 않고 인증만 확장할 수 있었습니다.

## 5. "되긴 되는데 보기 불편한" UX 감각

어려움이라기보다, **직접 써 보고 나서야 보이는 안목**의 영역이었습니다.

* 디자인을 **부트스트랩 기본 → 오버워치 테마 → 디시/인벤형 구조**로 여러 번 다시 잡음
* 영웅 정보가 *"영어로만 나와서 뭔지 모르겠다"* → `locale=ko-kr` 한글화
* *"역할별로 나눠야겠다"* → 인게임식 돌격/공격/지원 로스터 + 필터 탭

➡ 요구사항에 없던 불편함은 **만든 사람이 직접 사용해 봐야** 드러났습니다.

---

# 🛠 기술 스택

| 분야               | 기술                |
| ---------------- | ----------------- |
| Language         | Java 17           |
| Framework        | Spring MVC 6      |
| Security         | Spring Security 6 |
| Persistence      | MyBatis 3         |
| Database         | Oracle XE         |
| View             | JSP / JSTL        |
| Frontend         | Bootstrap 5       |
| Build            | Maven             |
| Server           | Tomcat 11         |
| External API     | OverFast API · Blizzard 패치노트 |
| Face Recognition | LBPH (직접 구현)   |

---

# 🏗 Architecture

```
Browser
      │
DispatcherServlet
      │
 Controller
      │
  Service
      │
MyBatis Mapper
      │
   Oracle XE
```

인증

```
Form Login
OAuth2 Login
Face Login
      │
OwUserPrincipal
      │
Spring Security
      │
Application
```

---

# 📁 프로젝트 구조

```
src
 ├── member        # 회원 · 폼/소셜/얼굴 로그인
 ├── board         # 게시판
 ├── comment       # 댓글
 ├── party         # 구인구직
 ├── message       # 쪽지
 ├── notification  # 알림
 ├── report        # 신고
 ├── visit         # 방문 집계
 ├── game          # 게임 정보(영웅·맵·패치노트)
 ├── admin         # 관리자
 └── config        # OAuth 설정
```

---

# 🚀 실행 방법

### 1. Oracle XE 준비

```bash
sqlplus hr/hr@localhost:1521/xe @src/main/resources/sql/schema.sql
# add_oauth.sql, add_face.sql 등 마이그레이션도 함께 적용
```

### 2. OAuth Key 입력

`src/main/resources/oauth.properties`에 카카오 키 입력
(네이버·구글은 키를 채우면 버튼이 자동 노출)

### 3. Tomcat 실행

Eclipse → Maven Update → Tomcat 11 실행

### 4. 접속

```
http://localhost:8080
```

또는

```
http://underwatch.local:8080
```

> ⚠️ 얼굴 로그인의 웹캠은 보안 컨텍스트 전용이라 **`localhost:8080`**에서만 동작합니다.

---

# 📚 프로젝트 문서

자세한 구현 내용은 아래 문서를 참고하세요.

| 문서 | 내용 |
| --- | --- |
| [docs/Architecture.md](docs/Architecture.md) | 전체 구조·요청 흐름·단일 컨텍스트·레이어 |
| [docs/OAuth.md](docs/OAuth.md) | 카카오·네이버·구글 소셜 로그인 구현 |
| [docs/FaceLogin.md](docs/FaceLogin.md) | LBPH 직접 구현 1:N 얼굴 로그인 |
| [docs/Database.md](docs/Database.md) | 테이블 스키마·마이그레이션·ERD |
| [docs/DevelopmentStory.md](docs/DevelopmentStory.md) | 개발하며 겪은 문제와 해결 상세 |

---

# 🔧 향후 개선 사항

* HTTPS 적용
* WebAuthn 적용
* 얼굴 라이브니스 검출
* OAuth Secret 관리 개선
* API 캐싱 적용
* Redis 기반 알림 기능 개선

---

# 👨‍💻 개발자

한국폴리텍대학 AI소프트웨어과
Backend Developer Portfolio Project
