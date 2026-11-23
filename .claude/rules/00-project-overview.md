# Project Overview

## Purpose

- 목적: Java 21의 TCP Socket API를 직접 사용해 SMTP, POP3, IMAP의 핵심 동작을 구현하는 교육용 경량 이메일 시스템(minimail). 컴퓨터네트워크 Homework 2(팀 프로젝트).
- 과제 요건과 확정된 방향의 원천은 project-brain(`project-docs/00-project-decisions.md`)이다. 이 문서에는 옮겨 적지 않는다.
- 기능 명세(지원 명령어, 상태 머신, 스키마, 포트·타임아웃 값, 인증 방식 등)는 확정 전에 임의로 구현하지 않는다.

## Primary Stack

- Java 21, 순수 Java(서버 프레임워크 없음)
- Gradle wrapper(Groovy DSL) + 얇은 Makefile, 모듈 `server`, `client`
- 서버 패키징: `application` 플러그인의 `installDist`
- PostgreSQL(서버 내부에서만 JDBC), Flyway
- 배포: GitHub Actions → DockerHub(public) → EC2에 SSH 후 `docker run`
- 기본 패키지: `com.chuseok22.minimail` (`.server`, `.client`)

## Allowed libraries

- PG JDBC, HikariCP, Flyway, JUnit 5, Mockito, SLF4J + Logback
- 그 외 의존성은 필요해지는 시점에 사용자와 합의한 뒤 추가한다.
- 쓰지 않는 것: ORM(Hibernate/JPA), Lombok, DI·서버 프레임워크, Netty 등, 완제품 메일 서버
- 소켓·프로토콜 처리는 표준 `Socket`, `ServerSocket` 등으로 직접 구현한다.

## Roles

- 사람이 작성: `server/`, `client/`의 Java 소스(네트워크 과제 범위). 이해하고 설명하는 것이 목적이다.
- Claude가 작성: CI/CD, 문서, 빌드·배포 설정(Gradle, Makefile, Dockerfile), Flyway SQL.
- 리뷰와 설명은 Claude가 한다.
- Java 소스는 사용자가 명시적으로 요청한 경우에만 작성한다. 어느 쪽인지 애매하면 작성하기 전에 묻는다.
- 팀원이 막혔을 때: 개념·RFC 설명, 리뷰 코멘트, 버그 원인 분석, 의사코드, 짧은 예시는 자유롭게 한다. 완성된 Java 구현 코드는 요청이 있을 때만 준다.

## Run modes

- 로컬: 로컬 서버가 각자 띄운 로컬 PG(Docker)에 연결한다. repo에 로컬 PG용 compose는 없다.
- 원격: EC2의 서버 컨테이너가 EC2의 PG 컨테이너에 연결한다.
- 두 환경 모두 시연할 수 있어야 하며, 로컬이 어려우면 원격으로 보여 준다.
- 클라이언트는 접속 호스트·포트를 환경변수 또는 실행 인자로 받는다.

## Main commands

- `make build`: 전체 빌드
- `make run-server`: 서버 실행(`server/.env.development`를 읽음)
- `make run-client`: 클라이언트 실행(`client/.env.development`를 읽음)
- 일부 테스트: `./gradlew :server:test --tests '*ClassName'`
- JDK 21이 필요하다. Gradle toolchain이 자동으로 내려받지 않는다.

## Project-specific constraints

- 제출물은 소스, Makefile, README다. 제출 소스에서 빌드와 실행이 재현되어야 한다.
- 네트워크 구현 코드는 팀원이 이해하고 설명할 수 있어야 한다.
- RFC 준수와 과제용 축소 구현을 구분한다.

## Change policy

- 요청받은 범위만 수정한다. 인접 코드를 리팩터링하지 않는다.
- 새 의존성, 새 모듈, 규칙 변경은 사용자와 합의한 뒤 한다.
