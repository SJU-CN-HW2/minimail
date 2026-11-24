package com.chuseok22.minimail.server.database;

import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerErrorCode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 경계와 연결 접근을 한 곳에서 맡는 관리자.
 *
 * <p>저장소는 {@link Connection}을 직접 받지 않고 {@link #withConnection}으로 연결을 쓴다. 현재 스레드에 진행 중인
 * 트랜잭션이 있으면 그 연결을 쓰고, 없으면 자동 커밋 상태의 새 연결을 쓴다.
 */
@Slf4j
@RequiredArgsConstructor
public final class TransactionManager {

  private final DataSource dataSource;
  private final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();

  /**
   * 작업을 하나의 트랜잭션으로 실행한다. 이미 트랜잭션이 있으면 합류한다. 안쪽 {@code execute}가 던진 예외를 바깥에서 잡아
   * 삼키지 말 것(바깥이 커밋한다).
   */
  public <T> T execute(Supplier<T> work) {
    if (currentConnection.get() != null) {
      return work.get();
    }
    Connection connection = openConnection();
    boolean rollbackFailed = false;
    try {
      connection.setAutoCommit(false);
      currentConnection.set(connection);
      T result;
      try {
        result = work.get();
      } catch (RuntimeException | Error exception) {
        rollbackFailed = !rollbackQuietly(connection, exception);
        throw exception;
      }
      try {
        connection.commit();
      } catch (SQLException exception) {
        rollbackFailed = !rollbackQuietly(connection, exception);
        throw new MailServerException(ServerErrorCode.DATABASE_QUERY_FAILED, exception);
      }
      return result;
    } catch (SQLException exception) {
      throw new MailServerException(ServerErrorCode.DATABASE_QUERY_FAILED, exception);
    } finally {
      currentConnection.remove();
      release(connection, rollbackFailed);
    }
  }

  /** 값을 돌려주지 않는 작업을 하나의 트랜잭션으로 실행한다. */
  public void executeWithoutResult(Runnable work) {
    execute(
        () -> {
          work.run();
          return null;
        });
  }

  /**
   * 연결을 받아 작업을 실행한다. 진행 중인 트랜잭션이 있으면 그 연결을 닫지 않고 쓰고, 없으면 새 연결을 쓰고 닫는다. {@link
   * SQLException}은 {@link ServerErrorCode#DATABASE_QUERY_FAILED}로 변환한다.
   */
  public <T> T withConnection(SqlFunction<Connection, T> work) {
    Connection bound = currentConnection.get();
    if (bound != null) {
      return apply(work, bound);
    }
    Connection connection = openConnection();
    try {
      return apply(work, connection);
    } finally {
      closeQuietly(connection);
    }
  }

  private <T> T apply(SqlFunction<Connection, T> work, Connection connection) {
    try {
      return work.apply(connection);
    } catch (SQLException exception) {
      throw new MailServerException(ServerErrorCode.DATABASE_QUERY_FAILED, exception);
    }
  }

  private Connection openConnection() {
    try {
      return dataSource.getConnection();
    } catch (SQLException exception) {
      throw new MailServerException(ServerErrorCode.DATABASE_QUERY_FAILED, exception);
    }
  }

  /** 롤백을 시도한다. 실패하면 원래 예외에 suppressed로 달고 {@code false}를 돌려준다. */
  private boolean rollbackQuietly(Connection connection, Throwable original) {
    try {
      connection.rollback();
      return true;
    } catch (SQLException rollbackFailure) {
      original.addSuppressed(rollbackFailure);
      log.warn("롤백에 실패했습니다", rollbackFailure);
      return false;
    }
  }

  /** 롤백이 실패한 경우 자동 커밋 복구를 건너뛴다(트랜잭션 도중 복구하면 커밋을 시도하기 때문). */
  private void release(Connection connection, boolean rollbackFailed) {
    if (!rollbackFailed) {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException exception) {
        log.warn("자동 커밋 복구에 실패했습니다", exception);
      }
    }
    closeQuietly(connection);
  }

  private void closeQuietly(Connection connection) {
    try {
      connection.close();
    } catch (SQLException exception) {
      log.warn("연결을 닫지 못했습니다", exception);
    }
  }
}
