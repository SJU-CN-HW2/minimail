-- 회원 계정 테이블을 만든다.
CREATE TABLE member (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  address text NOT NULL UNIQUE CHECK (address = lower(address)),
  password text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);
