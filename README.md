# UnderWatch

Spring MVC 6, Spring Security 6, MyBatis, Oracle XE 기반의 오버워치 커뮤니티 웹 서비스입니다.

## 주요 기능

- 폼 로그인, OAuth2 로그인, 얼굴 인식 로그인 통합
- 회원 게시글과 비회원 게시글 작성/수정/삭제
- 게시글과 댓글 추천/비추천, 댓글/대댓글, 신고
- 쪽지, 알림, WebSocket 기반 실시간 배지 갱신
- 마이페이지, 프로필 이미지, 회원 탈퇴 7일 유예와 자동 정리
- 관리자 대시보드, 회원/카테고리/신고 관리, 신고 처리 결과 쪽지 발송
- OverFast API와 Blizzard 패치노트 기반 게임 정보 제공

## 실행 요약

1. Oracle XE에 `src/main/resources/sql/schema.sql`과 `add_*.sql` 마이그레이션을 적용합니다.
2. `src/main/resources/oauth.properties`에 필요한 OAuth 키를 입력합니다.
3. Eclipse에서 Maven Update 후 Tomcat 11로 실행합니다.
4. 브라우저에서 `http://localhost:8080`으로 접속합니다.

얼굴 로그인은 브라우저 카메라 정책 때문에 로컬 개발 시 `localhost` 접속을 권장합니다.

## 문서

상세 기능, 구조, DB, 트러블슈팅은 [docs/README.md](docs/README.md)를 기준으로 확인하세요.
