package com.chuseok22.minimail.server;

import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerErrorCode;
import com.chuseok22.minimail.server.config.ServerConfig;
import com.chuseok22.minimail.server.database.DataSourceFactory;
import com.chuseok22.minimail.server.database.DatabaseMigrator;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 서버 진입점. 설정, 커넥션 풀, 마이그레이션 순으로 조립한다. */
public final class ServerMain {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerMain.class);

  private static final int FAILURE_EXIT_CODE = 1;

  private ServerMain() {}

  public static void main(String[] arguments) {
    try {
      start();
    } catch (MailServerException exception) {
      exitWithFailure(exception);
    } catch (RuntimeException exception) {
      exitWithFailure(new MailServerException(ServerErrorCode.INTERNAL_ERROR, exception));
    }
  }

  private static void exitWithFailure(MailServerException exception) {
    LOGGER.error("서버 시작에 실패했습니다. errorCode={}", exception.getErrorCode(), exception);
    System.exit(FAILURE_EXIT_CODE);
  }

  private static void start() {
    ServerConfig config = ServerConfig.fromEnvironment();
    try (HikariDataSource dataSource = DataSourceFactory.create(config)) {
      DatabaseMigrator.migrate(dataSource);
      LOGGER.info("서버 시작 준비 완료");
    }
  }
}
