package com.chuseok22.minimail.server;

import com.chuseok22.minimail.server.common.GlobalExceptionHandler;
import com.chuseok22.minimail.server.config.ServerConfig;
import com.chuseok22.minimail.server.database.DataSourceFactory;
import com.chuseok22.minimail.server.database.DatabaseMigrator;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 서버 진입점. 설정, 커넥션 풀, 마이그레이션 순으로 조립한다. */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerMain {

  private static final int FAILURE_EXIT_CODE = 1;

  public static void main(String[] arguments) {
    try {
      start();
    } catch (RuntimeException exception) {
      log.error("서버 시작에 실패했습니다.");
      GlobalExceptionHandler.handle(exception);
      System.exit(FAILURE_EXIT_CODE);
    }
  }

  private static void start() {
    ServerConfig config = ServerConfig.fromEnvironment();
    try (HikariDataSource dataSource = DataSourceFactory.create(config)) {
      DatabaseMigrator.migrate(dataSource);
      log.info("서버 시작 준비 완료");
    }
  }
}
