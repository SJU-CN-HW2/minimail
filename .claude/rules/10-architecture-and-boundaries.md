# Architecture And Boundaries

세부 패키지 구조와 추상화 범위는 구현 이슈를 시작할 때 정한다. 여기에는 확정된 경계만 둔다.

## High-level architecture

```text
[client(Java)] --TCP(SMTP/POP3/IMAP)--> [server(Java 21)] --JDBC--> [PostgreSQL]
```

- `server`와 `client`는 별도 Gradle 모듈이다. 공통 모듈은 처음에 만들지 않는다. 겹치는 코드가 생기면 그때 합의한다.
- SMTP, POP3, IMAP은 각각 별도 TCP 포트에서 리스너를 연다. 포트 값은 환경변수로 받는다.
- 세 프로토콜은 같은 메일 데이터와 메일함 상태(저장 계층)를 공유한다.

## Module boundaries

- 프로토콜 세션 코드는 SQL을 직접 쓰지 않고 저장 계층을 통해서만 DB에 접근한다.
- `client`는 DB와 `server` 내부 코드에 의존하지 않는다.
- 프로토콜 구현끼리 직접 의존하지 않는다. 공통 로직은 저장 계층이나 공용 코드로 올린다.

## Concurrency

- 연결 하나당 세션 하나이며 가상 스레드와 블로킹 `Socket` 입출력을 쓴다(NIO `Selector`는 쓰지 않는다).
- 최대 동시 연결 수 제한과 소켓 읽기 타임아웃은 필수다. 값은 기능 명세에서 정한다.
- DB 접근은 HikariCP 커넥션 풀을 거친다.

## Error flow

- 서버 내부 오류는 `ServerErrorCode` enum(오류 메시지 템플릿 `errorMessage` 포함) 하나와 `MailServerException` 하나로 표현한다. 클라이언트는 `ClientErrorCode`와 `MailClientException`으로 표현하며, 둘은 서로 참조하지 않는다.
- 각 프로토콜 세션 루프가 `ServerErrorCode`를 자기 응답 형식으로 번역한다(SMTP 응답 코드, POP3 `-ERR`, IMAP `NO`/`BAD`).
- 예상하지 못한 예외는 프로토콜의 일반 오류 응답으로 바꾸고, 스택·SQL 등 내부 정보는 클라이언트에 보내지 않는다.

## Configuration

- 설정은 환경변수로만 받는다. 시작 시 설정을 읽는 한 곳(클래스 이름은 구현 이슈에서 정한다)에서 읽고 검증한다.
- 필수 값이 비어 있으면 서버를 시작하지 않는다(fail fast).
- 코드 곳곳에서 `System.getenv`를 직접 호출하지 않는다.

## Database

- 스키마 변경은 Flyway 마이그레이션으로만 한다. 서버 시작 시 적용한다. 위치: `server/src/main/resources/db/migration/`
- 파일명: `V{yyyyMMdd}_{HHmmss}__{설명}.sql`
- 시드 데이터는 `R__{설명}.sql` 반복 마이그레이션으로 두고 멱등하게(`ON CONFLICT`) 작성한다. 내용이 바뀌면 다시 적용된다.
- 적용된 버전(`V`) 마이그레이션은 수정하지 않고 새 파일로 바꾼다.
