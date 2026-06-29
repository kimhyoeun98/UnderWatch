-- ============================================================
-- M-03 소셜 로그인: provider / provider_id 컬럼 추가
-- Run: sqlplus hr/hr@localhost:1521/xe @add_oauth.sql
-- ============================================================

-- 1) provider / provider_id 컬럼 추가
BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE ow_member ADD (provider VARCHAR2(20), provider_id VARCHAR2(100))';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -1430 THEN RAISE; END IF;   -- ORA-01430: column already exists
END;
/

-- 2) 소셜 계정은 비밀번호가 없으므로 password 를 NULL 허용으로 변경
BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE ow_member MODIFY (password VARCHAR2(100) NULL)';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -1451 THEN RAISE; END IF;   -- ORA-01451: column already NULL
END;
/

-- 3) 같은 제공자의 동일 계정 중복 방지
BEGIN
  EXECUTE IMMEDIATE 'CREATE UNIQUE INDEX uq_member_provider ON ow_member(provider, provider_id)';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -955 THEN RAISE; END IF;    -- ORA-00955: name already used
END;
/

COMMIT;

SELECT 'oauth columns ready' AS message FROM DUAL;
