# Team Conventions

## Naming

- 약어를 쓰지 않는다(`id`, `url` 제외). 이름은 의도를 드러내는 영어로 짓는다.
- 클래스는 PascalCase, 메서드·변수는 camelCase, 상수는 UPPER_SNAKE_CASE.
- 주석과 Javadoc은 한국어, 식별자는 영어.

## Code style

- Google Java Style: 들여쓰기 2칸, 줄 길이 100자.
- `.editorconfig`, 자동 포맷 도구, CI 포맷 검사는 두지 않는다. IDE 설정 파일은 사용자가 팀원에게 따로 전달한다.
- 불변 객체(`record`, `final`)를 권장한다.
- Lombok을 쓰지 않는다.
- 사용하지 않는 코드를 두지 않는다.
- 서버 코드에서 `System.out`과 `printStackTrace`를 쓰지 않는다. 클라이언트가 사용자에게 보여 주는 출력은 예외다.

## Design principles

- 하드코딩을 하지 않는다(지양). 값은 코드 안의 상수 한 곳에서 관리하거나 env 파일에 둔다. 같은 값을 두 곳에 적지 않는다. 단, 모듈별 env 파일에 같은 키(포트 등)가 양쪽에 있는 것은 허용한다.
- 단일 책임 원칙을 지킨다. 클래스·메서드·파일은 한 가지 일만 맡는다.
- 유지보수하기 쉬운 코드를 쓴다. 이름만 보고 역할이 드러나게 하고, 추측성 확장을 두지 않는다.

## Error handling

- 오류는 `ErrorCode` + `MailServerException`으로 던지고, 각 프로토콜 세션 루프에서 한 번 번역한다.
- 예외를 삼키지 않는다. 처리하지 못하면 다시 던지거나 로그를 남긴다.
- 클라이언트에 내부 정보(스택, SQL, 경로)를 노출하지 않는다.
- 외부 입력(프로토콜 명령 줄, 메일 본문)은 경계에서 검증한다.

## Logging

- SLF4J + Logback, 콘솔 출력, 한 줄 `key=value` 텍스트(JSON 아님).
- 세션 ID, 프로토콜, 원격 주소를 MDC 필드(`sessionId`, `protocol`, `remote`)로 붙인다.
- 레벨: INFO(연결 수립·종료, 상태 전이, 인증 결과), DEBUG(원문 `C:`/`S:`), WARN(예상된 클라이언트 오류), ERROR(서버 내부 오류와 스택)
- 로그 레벨은 환경변수 `LOG_LEVEL`로 바꾼다. 시연 때 DEBUG로 올릴 수 있어야 한다.
- 민감정보 마스킹은 작성자가 처리한다.

## Configuration and secrets

- 환경변수 파일은 모듈별(`server/`, `client/`)이며 `.env.example`만 추적한다. `.env.development`, `.env.production`은 gitignore이며 각자 만든다.
- `KEY=value` 형식이고 공백·`$` 등이 든 값은 작은따옴표 `'...'`로 감싼다.
- 새 설정 키를 추가하면 설정 로딩 코드와 `.env.example`을 함께 고친다.
- 시크릿 값은 커밋, 로그, PR, 대화에 노출하지 않는다. 노출되면 즉시 알리고 폐기한다.
- 배포 이미지는 public이고 `.env.production`이 이미지에 포함된다. 이 프로젝트 전용 값만 두고 다른 서비스와 같은 비밀번호를 쓰지 않는다.

## Review expectations (Claude 리뷰 기준)

- 프로토콜 상태 전이와 RFC 근거, 오류 번역, 공유 상태의 스레드 안전성, 타임아웃과 연결 수 제한, 시크릿 노출, 팀원이 설명할 수 있는 코드인지.
- block 기준: 시크릿 노출, force git, 적용된 마이그레이션 수정, 내부 정보 노출.
