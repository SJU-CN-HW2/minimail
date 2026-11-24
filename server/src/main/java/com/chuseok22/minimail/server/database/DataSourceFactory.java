package com.chuseok22.minimail.server.database;

import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerConstants;
import com.chuseok22.minimail.server.common.ServerErrorCode;
import com.chuseok22.minimail.server.config.ServerConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/** 설정으로부터 HikariCP 데이터 소스를 만든다. */
public final class DataSourceFactory {

  private DataSourceFactory() {
  }

  /**
   * 커넥션 풀을 생성한다. 풀 생성 중 접속에 실패하면 {@code DATABASE_CONNECTION_FAILED}를 던진다.
   * 오류 메시지에는 설정 값(비밀번호 등)을 넣지 않는다.
   */
  public static HikariDataSource create(ServerConfig config) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl("jdbc:postgresql://" + config.databaseHost() + ":"
        + config.databasePort() + "/" + config.databaseName());
    hikariConfig.setUsername(config.databaseUser());
    hikariConfig.setPassword(config.databasePassword());
    hikariConfig.setMaximumPoolSize(ServerConstants.DATABASE_POOL_MAXIMUM_SIZE);
    hikariConfig.setConnectionTimeout(ServerConstants.DATABASE_CONNECTION_TIMEOUT_MILLISECONDS);
    try {
      return new HikariDataSource(hikariConfig);
    } catch (RuntimeException exception) {
      throw new MailServerException(ServerErrorCode.DATABASE_CONNECTION_FAILED, exception);
    }
  }
}
