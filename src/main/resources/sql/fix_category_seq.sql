-- ============================================================
-- Fix seq_ow_board_category_no: align to MAX(no)+1
-- (categories were inserted with explicit no in fix_korean.sql)
-- Run: sqlplus hr/hr@localhost:1521/xe @fix_category_seq.sql
-- ============================================================

DECLARE
  v_max NUMBER;
BEGIN
  SELECT NVL(MAX(no), 0) INTO v_max FROM ow_board_category;
  BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE seq_ow_board_category_no';
  EXCEPTION WHEN OTHERS THEN NULL;
  END;
  EXECUTE IMMEDIATE 'CREATE SEQUENCE seq_ow_board_category_no START WITH ' || (v_max + 1) || ' INCREMENT BY 1 NOCACHE';
END;
/

SELECT 'category seq reset to max+1' AS message FROM DUAL;
