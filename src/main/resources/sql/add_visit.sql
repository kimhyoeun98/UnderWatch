-- ============================================================
-- A-04 daily visit counter (session-based, 1 per day)
-- Run: sqlplus hr/hr@localhost:1521/xe @add_visit.sql
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_visit_daily CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

CREATE TABLE ow_visit_daily (
    visit_date  DATE   PRIMARY KEY,
    visit_count NUMBER DEFAULT 0
);

COMMIT;

SELECT 'ow_visit_daily ready' AS message FROM DUAL;
