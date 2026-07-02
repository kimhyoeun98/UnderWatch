-- ============================================================
-- B-02 비로그인(게스트) 게시글 작성 지원
-- Run: sqlplus hr/hr@localhost:1521/xe @add_guest_board.sql
--
-- 비로그인 작성 시 writer_id 는 NULL, 대신 guest_name(표시 이름)과
-- guest_password(BCrypt 해시)를 저장해 본인 수정/삭제를 검증한다.
-- ============================================================

-- writer_id 를 NULL 허용으로 변경 (게스트 글은 회원 FK 없음)
BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE ow_board MODIFY (writer_id NULL)';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 게스트 표시 이름 / 비밀번호 해시 컬럼 추가
BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE ow_board ADD (guest_name VARCHAR2(50), guest_password VARCHAR2(100))';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

COMMIT;

SELECT 'ow_board guest columns ready' AS message FROM DUAL;
