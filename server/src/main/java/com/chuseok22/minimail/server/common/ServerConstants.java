package com.chuseok22.minimail.server.common;

/** 환경과 무관한 서버 고정값을 모아 둔 상수 클래스. */
public final class ServerConstants {

  /** 데이터베이스 커넥션 풀 최대 크기. 임시값이며 기능 명세에서 확정한다. */
  public static final int DATABASE_POOL_MAXIMUM_SIZE = 10;

  /** 데이터베이스 커넥션 획득 제한 시간(밀리초). 임시값이며 기능 명세에서 확정한다. */
  public static final long DATABASE_CONNECTION_TIMEOUT_MILLISECONDS = 5_000;

  /** Flyway 마이그레이션 SQL 위치. */
  public static final String MIGRATION_LOCATION = "classpath:db/migration";

  private ServerConstants() {
  }
}
