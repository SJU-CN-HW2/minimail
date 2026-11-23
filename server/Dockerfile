# Java 버전은 빌드 시 --build-arg JAVA_VERSION으로 전달한다 (gradle.properties의 javaVersion이 원천).
ARG JAVA_VERSION
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine

WORKDIR /app

COPY build/install/server /app
COPY .env.production /app/.env.production

# 포트는 .env.production에서만 정의한다. 셸이 파일을 읽어 환경변수로 내보낸 뒤 서버를 실행한다.
ENTRYPOINT ["sh", "-c", "set -a; . /app/.env.production; set +a; exec /app/bin/server"]
