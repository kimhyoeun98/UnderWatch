-- ============================================================
-- Broken Hangul data repair (UNISTR = client-encoding safe)
-- Run: sqlplus hr/hr@localhost:1521/xe @fix_korean.sql
-- ============================================================

-- 1. Rebuild categories (gongji / jayu / question / tip)
DELETE FROM ow_board_category;
INSERT INTO ow_board_category VALUES (1, UNISTR('\acf5\c9c0\c0ac\d56d'),      'Y', 1);
INSERT INTO ow_board_category VALUES (2, UNISTR('\c790\c720\ac8c\c2dc\d310'), 'N', 2);
INSERT INTO ow_board_category VALUES (3, UNISTR('\c9c8\bb38\b2f5\bcc0'),      'N', 3);
INSERT INTO ow_board_category VALUES (4, UNISTR('\d301\00b7\b178\d558\c6b0'), 'N', 4);

-- 2. Fix admin nickname/name
UPDATE ow_member
   SET nickname = UNISTR('\ad00\b9ac\c790'),
       name     = UNISTR('\ad00\b9ac\c790')
 WHERE id = 'admin';

-- 3. Ensure test member
MERGE INTO ow_member m
USING (SELECT 'testuser' AS id FROM dual) s
   ON (m.id = s.id)
 WHEN MATCHED THEN
   UPDATE SET nickname = UNISTR('\d14c\c2a4\d2b8\c720\c800'),
              name     = UNISTR('\d64d\ae38\b3d9')
 WHEN NOT MATCHED THEN
   INSERT (id, password, nickname, name, email)
   VALUES ('testuser',
           '$2a$10$slYQmyNdgTY18LWKqfQhIe5K4lMuXkOELEMNnmFrM9X7PKB1ZiQtS',
           UNISTR('\d14c\c2a4\d2b8\c720\c800'),
           UNISTR('\d64d\ae38\b3d9'),
           'test@underwatch.kr');

COMMIT;

SELECT no, sort_order FROM ow_board_category ORDER BY no;
