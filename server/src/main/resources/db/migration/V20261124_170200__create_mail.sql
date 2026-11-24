-- 메일함에 저장되는 메일 테이블을 만든다.
CREATE TABLE mail (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  mailbox_id uuid NOT NULL REFERENCES mailbox (id) ON DELETE CASCADE,
  uid bigint NOT NULL CHECK (uid >= 1),
  raw_message bytea NOT NULL,
  size_octets bigint NOT NULL CHECK (size_octets >= 0),
  received_at timestamptz NOT NULL DEFAULT now(),
  seen boolean NOT NULL DEFAULT false,
  deleted boolean NOT NULL DEFAULT false,
  UNIQUE (mailbox_id, uid)
);
