-- ============================================================
-- R-02 notification table
-- Run: sqlplus hr/hr@localhost:1521/xe @add_notification.sql
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_notification CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_ow_notification_no';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

CREATE TABLE ow_notification (
    no       NUMBER        PRIMARY KEY,
    user_id  VARCHAR2(20)  NOT NULL,
    content  VARCHAR2(500),
    link     VARCHAR2(500),
    is_read  CHAR(1)       DEFAULT 'N',
    reg_date DATE          DEFAULT SYSDATE,
    CONSTRAINT fk_ow_noti_member FOREIGN KEY (user_id) REFERENCES ow_member(id)
);

CREATE SEQUENCE seq_ow_notification_no START WITH 1 INCREMENT BY 1 NOCACHE;

COMMIT;

SELECT 'ow_notification ready' AS message FROM DUAL;
