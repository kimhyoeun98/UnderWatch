-- ============================================================
-- I-03 board vote table (one vote per member per board)
-- Run: sqlplus hr/hr@localhost:1521/xe @add_vote.sql
-- vote_type: L = like, D = dislike
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_board_vote CASCADE CONSTRAINTS';
  EXCEPTION WHEN OTHERS THEN NULL;
END;
/

CREATE TABLE ow_board_vote (
    member_id VARCHAR2(20) NOT NULL,
    board_no  NUMBER       NOT NULL,
    vote_type CHAR(1)      NOT NULL,
    reg_date  DATE         DEFAULT SYSDATE,
    CONSTRAINT pk_ow_board_vote  PRIMARY KEY (member_id, board_no),
    CONSTRAINT fk_ow_vote_member FOREIGN KEY (member_id) REFERENCES ow_member(id),
    CONSTRAINT fk_ow_vote_board  FOREIGN KEY (board_no)  REFERENCES ow_board(no) ON DELETE CASCADE
);

COMMIT;

SELECT 'ow_board_vote created' AS message FROM DUAL;
