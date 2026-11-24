package com.chuseok22.minimail.server.database;

import java.sql.SQLException;

/** {@link SQLException}을 던질 수 있는 함수형 인터페이스. */
@FunctionalInterface
public interface SqlFunction<T, R> {

  R apply(T value) throws SQLException;
}
