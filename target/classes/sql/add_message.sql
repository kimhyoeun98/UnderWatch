-- ============================================================
-- R-01 1:1 message table
-- Run: sqlplus hr/hr@localhost:1521/xe @add_message.sql
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_message CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_ow_message_no';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

CREATE TABLE ow_message (
    no          NUMBER        PRIMARY KEY,
    sender_id   VARCHAR2(20)  NOT NULL,
    receiver_id VARCHAR2(20)  NOT NULL,
    content     VARCHAR2(1000) NOT NULL,
    is_read     CHAR(1)       DEFAULT 'N',
    reg_date    DATE          DEFAULT SYSDATE,
    CONSTRAINT fk_ow_msg_sender   FOREIGN KEY (sender_id)   REFERENCES ow_member(id),
    CONSTRAINT fk_ow_msg_receiver FOREIGN KEY (receiver_id) REFERENCES ow_member(id)
);

CREATE SEQUENCE seq_ow_message_no START WITH 1 INCREMENT BY 1 NOCACHE;

COMMIT;

SELECT 'ow_message ready' AS message FROM DUAL;
