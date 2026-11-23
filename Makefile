SERVER_ENV_FILE ?= server/.env.development
CLIENT_ENV_FILE ?= client/.env.development

.PHONY: build run-server run-client

build:
	./gradlew build

run-server:
	@test -f $(SERVER_ENV_FILE) || { echo "$(SERVER_ENV_FILE) 파일이 없습니다. server/.env.example을 복사해 만드세요."; exit 1; }
	set -a; . $(SERVER_ENV_FILE); set +a; ./gradlew :server:run

run-client:
	@test -f $(CLIENT_ENV_FILE) || { echo "$(CLIENT_ENV_FILE) 파일이 없습니다. client/.env.example을 복사해 만드세요."; exit 1; }
	set -a; . $(CLIENT_ENV_FILE); set +a; ./gradlew :client:run --console=plain
