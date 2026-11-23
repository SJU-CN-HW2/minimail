# Delivery And Review

## Branch and commit

- 모든 작업은 이슈 기반이다. 이슈를 만들면 브랜치가 자동으로 생성된다. 브랜치 이름 규칙은 따로 두지 않는다.
- `main`에서 직접 작업하지 않는다. 이슈 브랜치에서 작업하고 `main`에 병합한다.
- 커밋 메시지는 `<type>: <한국어 설명>`이다(type: feat, fix, refactor, docs, test, chore, perf, ci). 이슈의 커밋 템플릿이 있으면 그것을 따른다.
- 스테이징은 경로를 지정한다(`git add <경로>`). `git add -A`와 `git add .`은 쓰지 않는다.
- 금지: `push --force`/`-f`/`--force-with-lease`, `git add -f`(`.gitignore` 우회 포함), `checkout -f`, `clean -f`, `branch -D`, `reset --hard`, 공유 이력의 `commit --amend`, `--no-verify`, `--no-gpg-sign`. 사용자가 그 순간 정확히 그 작업을 요청한 경우만 예외이며 서브에이전트에도 적용한다.
- `.gitignore`로 제외된 파일은 제외된 상태를 유지한다. 추적이 필요해 보이면 먼저 사용자에게 확인한다.

## Pull request

- PR 승인 규칙과 브랜치 보호 규칙은 두지 않는다.
- PR 전에 변경 범위의 검증 명령을 통과시킨다(`30-testing-and-verification.md`).
- 본문에는 변경 요약, 검증 결과, 확인이 필요한 사항을 적고 `Closes #N`으로 이슈를 연결한다.

## Delivery constraints

- **`main` 푸시(PR 병합 포함)는 곧 배포다.**
- **사용자가 그 순간 명시적으로 지시한 경우에만 `main`에 푸시하거나 병합한다.**
- 배포는 GitHub Actions가 한다: Gradle 빌드, secret `ENV_FILE`을 `server/.env.production`으로 써서 이미지에 포함, DockerHub 푸시, EC2에 SSH로 접속해 `docker run`.
- 필요한 GitHub Secrets(이름만 적는다): `ENV_FILE`, `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `SERVER_HOST`, `SERVER_PORT`, `SERVER_USER`, `SERVER_PASSWORD`. 값은 사용자가 GitHub Settings에 직접 등록한다.
- 서버는 TCP 포트 여러 개를 쓴다. `docker run`에서 포트마다 `-p`를 매핑한다.
- 배포 실패 시 이전 컨테이너가 이미 삭제되어 서버가 내려간 상태가 된다(태그가 `latest` 하나라 롤백 이미지가 없다). 병합 전에 `make run-server`로 서버가 뜨는지 확인한다.
- `ENV_FILE`의 `DB_HOST`는 `localhost`가 아니다(컨테이너 자기 자신을 가리킨다). EC2의 PG에 닿는 주소(호스트 IP 또는 docker0 게이트웨이)를 쓴다.
- EC2 보안 그룹에서 PG 포트(5432)를 외부에 열지 않는다. 이미지가 public이라 DB 비밀번호가 노출될 수 있다.
- `main`에는 릴리스 워크플로우가 `[skip ci]` 커밋을 추가할 수 있다. 푸시 전에 `git pull`로 먼저 반영한다.
- 버전은 커밋 제목으로 판정한다(`feat:` → minor, 그 외 → patch). `version.yml`을 직접 고치지 않는다.
- CI/CD는 `project-auto-wizard`가 만든 워크플로우를 최소한으로 수정해서 쓴다. `PROJECT-COMMON-*.yaml`은 건드리지 않는다.

## Records

- 작업이 끝나면 `../project-brain`에 기록한다. 기록 방식(형식, 시점, 도구)은 아직 정하지 않았다. 정해지면 이 절을 갱신한다.
- README에는 팀 정보와 팀원별 기여 내역을 적는다. 작업 기록에서 모은다.
