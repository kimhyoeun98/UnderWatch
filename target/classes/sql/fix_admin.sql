-- ============================================================
-- admin 계정 비밀번호 복구 (admin1234 BCrypt 해시)
-- Run: sqlplus hr/hr@localhost:1521/xe @fix_admin.sql
-- nickname/name '관리자' = UNISTR('\ad00\b9ac\c790')
-- ============================================================

MERGE INTO ow_member m
USING (SELECT 'admin' AS id FROM dual) s
   ON (m.id = s.id)
 WHEN MATCHED THEN
   UPDATE SET password = '$2b$10$ngY6kBlGH30cjkVp3FnU4uieZ2R.tjsED0E5Wr5UoSa0bBFMZgTQ2',
              role     = 'ROLE_ADMIN',
              status   = 'ACTIVE'
 WHEN NOT MATCHED THEN
   INSERT (id, password, nickname, name, email, role)
   VALUES ('admin',
           '$2b$10$ngY6kBlGH30cjkVp3FnU4uieZ2R.tjsED0E5Wr5UoSa0bBFMZgTQ2',
           UNISTR('\ad00\b9ac\c790'),
           UNISTR('\ad00\b9ac\c790'),
           'admin@underwatch.kr',
           'ROLE_ADMIN');

COMMIT;

SELECT id || ' | ' || role || ' | ' || status AS account FROM ow_member ORDER BY id;
