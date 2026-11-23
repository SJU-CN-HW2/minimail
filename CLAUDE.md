# CLAUDE.md

이 파일은 진입점이며, 상세 규칙은 `.claude/rules/*.md`에 있다. 작업 시작 전 아래 파일들을 읽고 진행한다.

- `.claude/rules/00-project-overview.md`: 목적, 스택, 허용 라이브러리, 역할, 실행 모드, 프로젝트 제약
- `.claude/rules/10-architecture-and-boundaries.md`: 모듈 경계, 동시성, 오류 흐름, 설정, DB
- `.claude/rules/20-team-conventions.md`: 네이밍, 코드 스타일, 설계 원칙, 오류·로깅, 설정·시크릿, 리뷰 기준
- `.claude/rules/30-testing-and-verification.md`: 테스트 정책(전역 80% 규칙을 대체), 검증 명령, 증거
- `.claude/rules/40-delivery-and-review.md`: 브랜치·커밋·PR, force 금지, 배포·CI/CD, 기록

## project-brain 읽기

회의 내용, 과제 요건, 평가 기준, 교훈 등은 project-brain에 정리되어 있다. 작업 전에 project-brain을 읽는다.

- 원격: https://github.com/SJU-CN-HW2/project-brain
- 로컬에 형제 디렉터리 `../project-brain`이 있으면 그것을 읽는다. 팀원은 형제 디렉터리에 없을 수 있으므로, 없으면 원격을 읽는다.

## 작업 방식

- main에서 작업하지 않는다.
- 모든 작업은 이슈, 브랜치 기반으로 한다.

## 문서 운영

- 규칙 문서에는 변하지 않는 공통 원칙만 둔다. 기능 명세(지원 명령어, 스키마, 포트 값 등)는 적지 않고 각 이슈 구현 전 브레인스토밍에서 결정한다.
- 결정과 근거는 project-brain에 있다. 문서와 코드가 어긋나면 코드가 맞다.
- 이슈 본문은 브레인스토밍을 시작할 때 맥락으로 한 번 가볍게 읽는 용도다.

## 절대 금지

- force 계열 git 명령(`--force`, `git add -f` 등)
- 사용자가 그 순간 명시적으로 지시하지 않은 `main` 푸시(직접 푸시, PR 병합 모두). `main` 푸시는 곧 배포다
- `.env.development`, `.env.production`, 키·인증서·시크릿 값 커밋 또는 노출
- 합의되지 않은 의존성 추가, 확정되지 않은 기능 명세의 임의 구현
- 약어 사용(`id`, `url` 제외)
