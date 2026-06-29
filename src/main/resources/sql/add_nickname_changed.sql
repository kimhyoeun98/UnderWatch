-- ============================================================
-- M-07 nickname change cooldown column
-- Run: sqlplus hr/hr@localhost:1521/xe @add_nickname_changed.sql
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE ow_member ADD (nickname_changed DATE)';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -1430 THEN RAISE; END IF;   -- ORA-01430: column already exists
END;
/

COMMIT;

SELECT 'nickname_changed column ready' AS message FROM DUAL;
