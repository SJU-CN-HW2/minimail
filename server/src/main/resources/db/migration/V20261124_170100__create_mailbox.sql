-- 회원별 메일함 테이블을 만든다.
CREATE TABLE mailbox (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  member_id uuid NOT NULL REFERENCES member (id) ON DELETE CASCADE,
  name text NOT NULL,
  uid_validity bigint NOT NULL CHECK (uid_validity > 0),
  next_uid bigint NOT NULL DEFAULT 1 CHECK (next_uid >= 1),
  UNIQUE (member_id, name)
);
