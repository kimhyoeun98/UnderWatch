-- ============================================================
-- B-04 image columns on ow_board (path only; file on disk)
-- Run: sqlplus hr/hr@localhost:1521/xe @add_image.sql
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE ow_board ADD (image_stored VARCHAR2(100), image_orig VARCHAR2(200))';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -1430 THEN RAISE; END IF;   -- ORA-01430: column already exists
END;
/

COMMIT;

SELECT 'image columns ready' AS message FROM DUAL;
