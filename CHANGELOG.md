# Changelog

**현재 버전:** 0.1.0  
**마지막 업데이트:** 2026-11-24T06:53:12Z  

---

## [0.1.0] - 2026-11-24

**✨ 기능**
- 서버_클라이언트_기본_코드_골격_추가 — 클라이언트 설정 로딩과 진입점 추가
- 서버_클라이언트_기본_코드_골격_추가 — 서버 시작 조립(DB 풀, Flyway 마이그레이션) 추가
- 서버_클라이언트_기본_코드_골격_추가 — 서버 환경변수 설정 로딩과 검증 추가
- 서버_클라이언트_기본_코드_골격_추가 — 서버 공통 예외 ErrorCode와 MailServerException 추가

**📝 문서**
- lombok 사용 범위와 지양 목록 규칙 추가
- 오류 처리 규칙에 ServerErrorCode와 ClientErrorCode 반영

**♻️ 리팩토링**
- 서버_클라이언트_기본_코드_골격_추가 — 클라이언트 코드에 Lombok 적용
- 서버_클라이언트_기본_코드_골격_추가 — 서버 코드에 Lombok 적용
- 서버_클라이언트_기본_코드_골격_추가 — 클라이언트 오류 코드와 메시지를 ClientErrorCode로 중앙화
- 서버_클라이언트_기본_코드_골격_추가 — 서버 오류 코드와 메시지를 ServerErrorCode로 중앙화

**🔧 변경사항**
- lombok 의존성 추가

---

## [0.0.4] - 2026-11-23

**📝 문서**
- db 테스트를 작성하지 않는 테스트 규칙으로 정리
- readme 골격 추가
- 프로젝트 규칙 문서 정리

**🔧 변경사항**
- gitignore에 작업 보고서 폴더 추가
- 배포 스크립트에 sudo 실행과 arm64 이미지 빌드 반영
- dbTest 태스크와 db 태그 제외 설정 제거
- 서버 ec2 자동 배포 워크플로우 추가
- pr 빌드·테스트 워크플로우 추가
- logback 로깅 설정 추가
- gradle 멀티 모듈 빌드 구성
- gitignore에 이슈 초안·계획 문서·gradle 산출물 경로 추가

---

## [0.0.3] - 2026-11-23

**🔧 변경사항**
- add github issue and pr templates

---

## [0.0.2] - 2026-11-23

**📝 문서**
- add project CLAUDE.md

**🔧 변경사항**
- add .gitignore
- add version.yml
- add github workflows and release scripts
- Initial commit

---

