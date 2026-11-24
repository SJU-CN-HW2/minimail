-- 시연용 일회용 계정과 기본 메일함을 넣는다. 내용을 바꾸면 다음 기동 때 다시 적용되며, 여러 번 적용해도 중복되지 않는다.
INSERT INTO member (address, password) VALUES
  ('alice@minimail.test', 'alice-demo'),
  ('bob@minimail.test', 'bob-demo'),
  ('carol@minimail.test', 'carol-demo')
ON CONFLICT (address) DO UPDATE SET password = EXCLUDED.password;

INSERT INTO mailbox (member_id, name, uid_validity)
SELECT member.id, names.name, extract(epoch FROM now())::bigint
FROM member CROSS JOIN (VALUES ('INBOX'), ('Sent'), ('Trash')) AS names(name)
WHERE member.address IN ('alice@minimail.test', 'bob@minimail.test', 'carol@minimail.test')
ON CONFLICT (member_id, name) DO NOTHING;
