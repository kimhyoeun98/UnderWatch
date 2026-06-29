-- ============================================================
-- ow_message 에 나만 삭제 컬럼 추가
-- Run: sqlplus hr/hr@localhost:1521/xe @add_message_delete.sql
-- ============================================================

ALTER TABLE ow_message ADD sender_deleted   CHAR(1) DEFAULT 'N';
ALTER TABLE ow_message ADD receiver_deleted CHAR(1) DEFAULT 'N';

COMMIT;

SELECT 'ow_message delete columns added' AS message FROM DUAL;
