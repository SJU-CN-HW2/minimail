package com.chuseok22.minimail.server.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** {@link ResultSet} 값 변환 보조 메서드 모음. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResultSets {

  /** NOT NULL인 {@code timestamptz} 컬럼을 {@link Instant}로 읽는다. */
  public static Instant getInstant(ResultSet resultSet, String column) throws SQLException {
    return resultSet.getObject(column, OffsetDateTime.class).toInstant();
  }
}
