-- =============================================================
-- UnderWatch 데이터베이스 스키마 (Oracle XE, hr 계정)
-- 실행 전 hr 계정으로 접속 필요: sqlplus hr/hr@localhost:1521:xe
-- =============================================================

-- 기존 테이블/시퀀스 정리 (재실행 시)
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_comment CASCADE CONSTRAINTS';
  EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_board CASCADE CONSTRAINTS';
  EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_board_category CASCADE CONSTRAINTS';
  EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_member CASCADE CONSTRAINTS';
  EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_ow_board_category_no';
  EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_ow_board_no';
  EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_ow_comment_no';
  EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- =============================================================
-- 1. 회원 테이블
-- =============================================================
CREATE TABLE ow_member (
    id          VARCHAR2(20)  PRIMARY KEY,
    password    VARCHAR2(100) NOT NULL,
    nickname    VARCHAR2(50)  NOT NULL UNIQUE,
    name        VARCHAR2(50)  NOT NULL,
    phone       VARCHAR2(20),
    email       VARCHAR2(100) UNIQUE,
    role        VARCHAR2(20)  DEFAULT 'ROLE_USER' NOT NULL,
    grade_point NUMBER        DEFAULT 0,
    profile_img VARCHAR2(500),
    status      VARCHAR2(20)  DEFAULT 'ACTIVE',
    reg_date    DATE          DEFAULT SYSDATE,
    update_date DATE          DEFAULT SYSDATE
);

-- 관리자 계정 (비밀번호: admin1234 BCrypt 해시)
INSERT INTO ow_member(id, password, nickname, name, email, role)
VALUES ('admin',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyU9WpKiS',
        '관리자', '관리자', 'admin@underwatch.kr', 'ROLE_ADMIN');

-- 테스트 회원 (비밀번호: user1234 BCrypt 해시)
INSERT INTO ow_member(id, password, nickname, name, email)
VALUES ('testuser',
        '$2a$10$slYQmyNdgTY18LWKqfQhIe5K4lMuXkOELEMNnmFrM9X7PKB1ZiQtS',
        '테스트유저', '홍길동', 'test@underwatch.kr');

COMMIT;

-- =============================================================
-- 2. 게시판 카테고리 테이블
-- =============================================================
CREATE TABLE ow_board_category (
    no           NUMBER        PRIMARY KEY,
    name         VARCHAR2(50)  NOT NULL,
    is_admin_only CHAR(1)      DEFAULT 'N',
    sort_order   NUMBER        DEFAULT 0
);

CREATE SEQUENCE seq_ow_board_category_no START WITH 1 INCREMENT BY 1 NOCACHE;

INSERT INTO ow_board_category VALUES (seq_ow_board_category_no.NEXTVAL, '공지사항',  'Y', 1);
INSERT INTO ow_board_category VALUES (seq_ow_board_category_no.NEXTVAL, '자유게시판', 'N', 2);
INSERT INTO ow_board_category VALUES (seq_ow_board_category_no.NEXTVAL, '질문답변',  'N', 3);
INSERT INTO ow_board_category VALUES (seq_ow_board_category_no.NEXTVAL, '팁·노하우', 'N', 4);
COMMIT;

-- =============================================================
-- 3. 게시글 테이블
-- =============================================================
CREATE TABLE ow_board (
    no          NUMBER         PRIMARY KEY,
    category_no NUMBER         NOT NULL,
    writer_id   VARCHAR2(20)   NOT NULL,
    title       VARCHAR2(200)  NOT NULL,
    content     CLOB           NOT NULL,
    view_cnt    NUMBER         DEFAULT 0,
    like_cnt    NUMBER         DEFAULT 0,
    dislike_cnt NUMBER         DEFAULT 0,
    is_deleted  CHAR(1)        DEFAULT 'N',
    reg_date    DATE           DEFAULT SYSDATE,
    update_date DATE           DEFAULT SYSDATE,
    CONSTRAINT fk_board_category FOREIGN KEY (category_no) REFERENCES ow_board_category(no),
    CONSTRAINT fk_board_writer   FOREIGN KEY (writer_id)   REFERENCES ow_member(id)
);

CREATE SEQUENCE seq_ow_board_no START WITH 1 INCREMENT BY 1 NOCACHE;

-- 성능 인덱스
CREATE INDEX idx_board_category ON ow_board(category_no);
CREATE INDEX idx_board_regdate  ON ow_board(reg_date DESC);
CREATE INDEX idx_board_writer   ON ow_board(writer_id);

-- =============================================================
-- 4. 댓글 테이블
-- =============================================================
CREATE TABLE ow_comment (
    no          NUMBER          PRIMARY KEY,
    board_no    NUMBER          NOT NULL,
    writer_id   VARCHAR2(20)    NOT NULL,
    parent_no   NUMBER,
    content     VARCHAR2(1000)  NOT NULL,
    is_deleted  CHAR(1)         DEFAULT 'N',
    reg_date    DATE            DEFAULT SYSDATE,
    CONSTRAINT fk_comment_board  FOREIGN KEY (board_no)  REFERENCES ow_board(no) ON DELETE CASCADE,
    CONSTRAINT fk_comment_writer FOREIGN KEY (writer_id) REFERENCES ow_member(id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_no) REFERENCES ow_comment(no)
);

CREATE SEQUENCE seq_ow_comment_no START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE INDEX idx_comment_board ON ow_comment(board_no);

COMMIT;

-- 완료 메시지
SELECT 'UnderWatch 스키마 생성 완료' AS message FROM DUAL;
