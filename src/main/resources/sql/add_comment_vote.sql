-- ============================================================
-- I-03 댓글 추천/비추천 (회원당 댓글 1표)
-- Run: sqlplus hr/hr@localhost:1521/xe @add_comment_vote.sql
-- vote_type: L = like, D = dislike
-- ============================================================

-- 댓글 추천/비추천 집계 컬럼
BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE ow_comment ADD (like_cnt NUMBER DEFAULT 0, dislike_cnt NUMBER DEFAULT 0)';
EXCEPTION WHEN OTHERS THEN NULL;  -- 이미 존재하면 무시
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ow_comment_vote CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

CREATE TABLE ow_comment_vote (
    member_id  VARCHAR2(20) NOT NULL,
    comment_no NUMBER       NOT NULL,
    vote_type  CHAR(1)      NOT NULL,
    reg_date   DATE         DEFAULT SYSDATE,
    CONSTRAINT pk_ow_comment_vote  PRIMARY KEY (member_id, comment_no),
    CONSTRAINT fk_ow_cvote_member  FOREIGN KEY (member_id)  REFERENCES ow_member(id),
    CONSTRAINT fk_ow_cvote_comment FOREIGN KEY (comment_no) REFERENCES ow_comment(no) ON DELETE CASCADE
);

COMMIT;

SELECT 'ow_comment_vote ready' AS message FROM DUAL;
