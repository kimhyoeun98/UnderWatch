-- ============================================================
-- M-10 계정 탈퇴: 비밀번호 확인 후 7일 보관 → 자동 삭제
-- Run: sqlplus hr/hr@localhost:1521/xe @add_withdraw.sql
--
-- status 값: ACTIVE(정상) / SUSPENDED(관리자 정지) / WITHDRAWN(탈퇴 대기)
-- withdraw_at: 탈퇴 신청 시각. 신청 후 7일이 지나면 스케줄러가 실제 삭제.
-- ============================================================

BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE ow_member ADD (withdraw_at DATE)';
EXCEPTION WHEN OTHERS THEN NULL;  -- 이미 존재하면 무시
END;
/

COMMIT;

SELECT 'ow_member.withdraw_at ready' AS message FROM DUAL;
