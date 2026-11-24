package com.chuseok22.minimail.server.database;

import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerConstants;
import com.chuseok22.minimail.server.common.ServerErrorCode;
import javax.sql.DataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.Flyway;

/** Flyway로 데이터베이스 스키마 마이그레이션을 적용한다. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseMigrator {

  /** 마이그레이션을 적용한다. 실패하면 {@code DATABASE_MIGRATION_FAILED}를 던진다. */
  public static void migrate(DataSource dataSource) {
    try {
      Flyway.configure()
          .dataSource(dataSource)
          .locations(ServerConstants.MIGRATION_LOCATION)
          .load()
          .migrate();
    } catch (RuntimeException exception) {
      throw new MailServerException(ServerErrorCode.DATABASE_MIGRATION_FAILED, exception);
    }
  }
}
