# Database

Oracle XE 기반 데이터베이스 구조 문서입니다.
모든 테이블 접두사는 `ow_`입니다.

---

## 1. 마이그레이션 적용 순서

| 순서 | 파일                         | 내용                |
| -- | -------------------------- | ----------------- |
| 1  | `schema.sql`               | 회원, 카테고리, 게시글, 댓글 |
| 2  | `add_vote.sql`             | 추천 / 비추천          |
| 3  | `add_image.sql`            | 게시글 이미지           |
| 4  | `add_report.sql`           | 신고                |
| 5  | `add_visit.sql`            | 방문 집계             |
| 6  | `add_party.sql`            | 구인구직              |
| 7  | `add_message.sql`          | 쪽지                |
| 8  | `add_notification.sql`     | 알림                |
| 9  | `add_nickname_changed.sql` | 닉네임 변경일           |
| 10 | `add_oauth.sql`            | 소셜 로그인            |
| 11 | `add_face.sql`             | 얼굴 로그인            |

실행 예시:

```bash
sqlplus hr/hr@localhost:1521/xe @src/main/resources/sql/schema.sql
```

---

## 2. ERD 요약

```text
ow_member ─┬─< ow_board >─┬─< ow_comment
           │              ├─< ow_board_vote
           │              └─ image columns
           ├─< ow_party
           ├─< ow_message
           ├─< ow_notification
           └─< ow_report
ow_board_category ─< ow_board
ow_visit_daily
```

---

## 3. 핵심 테이블

### ow_member

| 컬럼                | 설명            |
| ----------------- | ------------- |
| `id`              | 회원 ID         |
| `password`        | BCrypt 비밀번호   |
| `nickname`        | 닉네임           |
| `email`           | 이메일           |
| `role`            | 권한            |
| `grade_point`     | 활동 점수         |
| `profile_img`     | 프로필 이미지       |
| `status`          | 회원 상태         |
| `provider`        | 소셜 제공자        |
| `provider_id`     | 소셜 고유 ID      |
| `face_descriptor` | 얼굴 특징(LBPH 히스토그램) JSON |

---

### ow_board

| 컬럼             | 설명      |
| -------------- | ------- |
| `no`           | 게시글 번호  |
| `category_no`  | 카테고리 번호 |
| `writer_id`    | 작성자     |
| `title`        | 제목      |
| `content`      | 내용      |
| `view_cnt`     | 조회수     |
| `like_cnt`     | 추천 수    |
| `dislike_cnt`  | 비추천 수   |
| `image_stored` | 저장 이미지명 |
| `image_orig`   | 원본 이미지명 |

---

### ow_comment

| 컬럼           | 설명       |
| ------------ | -------- |
| `no`         | 댓글 번호    |
| `board_no`   | 게시글 번호   |
| `writer_id`  | 작성자      |
| `parent_no`  | 부모 댓글 번호 |
| `content`    | 댓글 내용    |
| `is_deleted` | 삭제 여부    |

---

## 4. 기능 테이블

| 테이블               | 설명           |
| ----------------- | ------------ |
| `ow_board_vote`   | 게시글 추천 / 비추천 |
| `ow_report`       | 게시글 / 댓글 신고  |
| `ow_party`        | 구인구직         |
| `ow_message`      | 쪽지           |
| `ow_notification` | 알림           |
| `ow_visit_daily`  | 일일 방문 집계     |

---

## 5. MyBatis 설정

* `mapUnderscoreToCamelCase = true`
* `jdbcTypeForNull = NULL`
* Mapper XML은 `config/sqlMap/oracle/*.xml`에 위치

---

## 6. Oracle 주의사항

* SQL*Plus 한글 인코딩 주의
* CDB / PDB 접속 대상 확인
* 재실행 시 시퀀스 충돌 가능
* 관리자 계정 해시는 별도 패치 스크립트로 보정 가능
