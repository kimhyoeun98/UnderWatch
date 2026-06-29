-- ============================================================
-- A-03 report table (target_type: B=board, C=comment)
-- Run: sqlplus hr/hr@localhost:1521/xe @add_report.sql
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_report CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_ow_report_no';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

CREATE TABLE ow_report (
    no          NUMBER       PRIMARY KEY,
    target_type CHAR(1)      NOT NULL,
    target_no   NUMBER       NOT NULL,
    reporter_id VARCHAR2(20) NOT NULL,
    reason      VARCHAR2(500),
    status      VARCHAR2(20) DEFAULT 'PENDING',
    reg_date    DATE         DEFAULT SYSDATE,
    CONSTRAINT fk_ow_report_member FOREIGN KEY (reporter_id) REFERENCES ow_member(id)
);

CREATE SEQUENCE seq_ow_report_no START WITH 1 INCREMENT BY 1 NOCACHE;

COMMIT;

SELECT 'ow_report ready' AS message FROM DUAL;
