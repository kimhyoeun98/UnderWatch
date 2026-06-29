-- ============================================================
-- M-09 얼굴 로그인: 얼굴 특징(LBPH 히스토그램) 저장 컬럼
-- Run: sqlplus hr/hr@localhost:1521/xe @add_face.sql
-- face_descriptor 에는 정규화 얼굴 벡터(PCA용 64x64=4096 정수 배열) JSON 문자열을 저장한다.
-- (CLOB 이므로 수천~수만 길이 정수 배열도 충분히 들어간다.)
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE ow_member ADD (face_descriptor CLOB)';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -1430 THEN RAISE; END IF;   -- ORA-01430: column already exists
END;
/

COMMIT;

SELECT 'face_descriptor column ready' AS message FROM DUAL;
