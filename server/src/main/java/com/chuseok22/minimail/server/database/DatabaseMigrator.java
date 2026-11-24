package com.chuseok22.minimail.server.database;

import com.chuseok22.minimail.server.common.ErrorCode;
import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerConstants;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

/** Flyway로 데이터베이스 스키마 마이그레이션을 적용한다. */
public final class DatabaseMigrator {

  private DatabaseMigrator() {
  }

  /** 마이그레이션을 적용한다. 실패하면 {@code DATABASE_FAILURE}를 던진다. */
  public static void migrate(DataSource dataSource) {
    try {
      Flyway.configure()
          .dataSource(dataSource)
          .locations(ServerConstants.MIGRATION_LOCATION)
          .load()
          .migrate();
    } catch (RuntimeException exception) {
      throw new MailServerException(
          ErrorCode.DATABASE_FAILURE, "데이터베이스 마이그레이션에 실패했습니다.", exception);
    }
  }
}
