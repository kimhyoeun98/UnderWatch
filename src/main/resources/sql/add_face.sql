-- ============================================================
-- M-09 얼굴 로그인: 얼굴 임베딩 저장 컬럼
-- Run: sqlplus hr/hr@localhost:1521/xe @add_face.sql
-- face_descriptor 에는 얼굴 API가 반환한 512차원 임베딩 목록(JSON 문자열)을 저장한다.
-- 한 회원당 최대 3장까지 저장하며, 로그인 시 등록 회원 전체와 1:N 코사인 비교한다.
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE ow_member ADD (face_descriptor CLOB)';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -1430 THEN RAISE; END IF;   -- ORA-01430: column already exists
END;
/

COMMIT;

SELECT 'face_descriptor column ready' AS message FROM DUAL;
