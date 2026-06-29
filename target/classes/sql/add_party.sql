-- ============================================================
-- P-01~03 party recruit table
-- Run: sqlplus hr/hr@localhost:1521/xe @add_party.sql
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_party CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_ow_party_no';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

CREATE TABLE ow_party (
    no        NUMBER        PRIMARY KEY,
    writer_id VARCHAR2(20)  NOT NULL,
    title     VARCHAR2(200) NOT NULL,
    content   VARCHAR2(2000),
    role_type VARCHAR2(30),
    tier      VARCHAR2(30),
    mic_yn    CHAR(1)       DEFAULT 'N',
    main_hero VARCHAR2(50),
    status    VARCHAR2(20)  DEFAULT 'RECRUITING',
    reg_date  DATE          DEFAULT SYSDATE,
    CONSTRAINT fk_ow_party_member FOREIGN KEY (writer_id) REFERENCES ow_member(id)
);

CREATE SEQUENCE seq_ow_party_no START WITH 1 INCREMENT BY 1 NOCACHE;

COMMIT;

SELECT 'ow_party ready' AS message FROM DUAL;
