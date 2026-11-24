package com.chuseok22.minimail.server.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 환경과 무관한 서버 고정값을 모아 둔 상수 클래스. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerConstants {

  /** 데이터베이스 커넥션 풀 최대 크기. 임시값이며 기능 명세에서 확정한다. */
  public static final int DATABASE_POOL_MAXIMUM_SIZE = 10;

  /** 데이터베이스 커넥션 획득 제한 시간(밀리초). 임시값이며 기능 명세에서 확정한다. */
  public static final long DATABASE_CONNECTION_TIMEOUT_MILLISECONDS = 5_000;

  public static final int MAXIMUM_CONCURRENT_CONNECTIONS = 100;

  public static final int SOCKET_READ_TIMEOUT_MILLISECONDS = 600_000;

  public static final int MAXIMUM_LINE_BYTES = 65_536;

  /** Flyway 마이그레이션 SQL 위치. */
  public static final String MIGRATION_LOCATION = "classpath:db/migration";

  /** 비밀번호 등 민감 값을 문자열로 출력할 때 대신 쓰는 마스킹 문자열. */
  public static final String MASKED_PASSWORD = "****";
}
