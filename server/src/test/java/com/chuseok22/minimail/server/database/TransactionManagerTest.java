package com.chuseok22.minimail.server.database;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerErrorCode;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class TransactionManagerTest {

  private DataSource dataSource;
  private Connection connection;
  private TransactionManager transactionManager;

  @BeforeEach
  void setUp() throws SQLException {
    dataSource = mock(DataSource.class);
    connection = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(connection);
    transactionManager = new TransactionManager(dataSource);
  }

  @Test
  void commitsWhenWorkSucceeds() throws SQLException {
    String result = transactionManager.execute(() -> "done");

    assertEquals("done", result);
    InOrder order = inOrder(connection);
    order.verify(connection).setAutoCommit(false);
    order.verify(connection).commit();
    order.verify(connection).setAutoCommit(true);
    order.verify(connection).close();
    verify(connection, never()).rollback();
  }

  @Test
  void rollsBackAndRethrowsSameException() throws SQLException {
    MailServerException failure = new MailServerException(ServerErrorCode.INTERNAL_ERROR);

    MailServerException thrown =
        assertThrows(
            MailServerException.class,
            () ->
                transactionManager.execute(
                    () -> {
                      throw failure;
                    }));

    assertSame(failure, thrown);
    verify(connection).rollback();
    verify(connection, never()).commit();
    verify(connection).close();
  }

  @Test
  void nestedExecuteJoinsOuterTransaction() throws SQLException {
    Connection[] innerConnection = new Connection[1];
    Connection[] outerConnection = new Connection[1];

    transactionManager.execute(
        () ->
            transactionManager.withConnection(
                outer -> {
                  outerConnection[0] = outer;
                  transactionManager.execute(
                      () ->
                          transactionManager.withConnection(
                              inner -> {
                                innerConnection[0] = inner;
                                return null;
                              }));
                  return null;
                }));

    assertSame(outerConnection[0], innerConnection[0]);
    verify(dataSource, times(1)).getConnection();
    verify(connection, times(1)).setAutoCommit(false);
    verify(connection, times(1)).commit();
  }

  @Test
  void threadBindingIsClearedAfterFailure() throws SQLException {
    Connection first = mock(Connection.class);
    Connection second = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(first, second);

    assertThrows(
        IllegalStateException.class,
        () ->
            transactionManager.execute(
                () -> {
                  throw new IllegalStateException();
                }));
    Connection received = transactionManager.withConnection(connectionInUse -> connectionInUse);

    assertSame(second, received);
    verify(first).close();
    verify(second).close();
  }

  @Test
  void withConnectionOutsideTransactionClosesConnection() throws SQLException {
    transactionManager.withConnection(connectionInUse -> "value");

    verify(connection).close();
    verify(connection, never()).setAutoCommit(false);
  }

  @Test
  void withConnectionInsideTransactionDoesNotClose() throws SQLException {
    transactionManager.execute(
        () -> {
          transactionManager.withConnection(connectionInUse -> "value");
          try {
            verify(connection, never()).close();
          } catch (SQLException exception) {
            throw new AssertionError(exception);
          }
          return null;
        });

    verify(connection, times(1)).close();
  }

  @Test
  void translatesSqlExceptionFromWork() {
    SQLException cause = new SQLException("boom");

    MailServerException thrown =
        assertThrows(
            MailServerException.class,
            () ->
                transactionManager.withConnection(
                    connectionInUse -> {
                      throw cause;
                    }));

    assertEquals(ServerErrorCode.DATABASE_QUERY_FAILED, thrown.getErrorCode());
    assertSame(cause, thrown.getCause());
  }

  @Test
  void translatesSqlExceptionOnCommit() throws SQLException {
    SQLException cause = new SQLException("commit failed");
    org.mockito.Mockito.doThrow(cause).when(connection).commit();

    MailServerException thrown =
        assertThrows(MailServerException.class, () -> transactionManager.execute(() -> "value"));

    assertEquals(ServerErrorCode.DATABASE_QUERY_FAILED, thrown.getErrorCode());
    assertSame(cause, thrown.getCause());
    verify(connection).rollback();
    verify(connection).close();
    Connection received = transactionManager.withConnection(connectionInUse -> connectionInUse);
    assertSame(connection, received);
    verify(dataSource, times(2)).getConnection();
  }

  @Test
  void propagatesRuntimeExceptionFromWithConnection() {
    MailServerException failure = new MailServerException(ServerErrorCode.MAILBOX_NOT_FOUND);

    MailServerException thrown =
        assertThrows(
            MailServerException.class,
            () ->
                transactionManager.withConnection(
                    connectionInUse -> {
                      throw failure;
                    }));

    assertSame(failure, thrown);
  }

  @Test
  void rollbackFailureDoesNotMaskOriginalException() throws SQLException {
    SQLException rollbackFailure = new SQLException("rollback failed");
    org.mockito.Mockito.doThrow(rollbackFailure).when(connection).rollback();
    IllegalStateException failure = new IllegalStateException("original");

    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                transactionManager.execute(
                    () -> {
                      throw failure;
                    }));

    assertSame(failure, thrown);
    assertArrayEquals(new Throwable[] {rollbackFailure}, thrown.getSuppressed());
    verify(connection, never()).setAutoCommit(true);
    verify(connection).close();
  }

  @Test
  void translatesSqlExceptionWhenGetConnectionFails() throws SQLException {
    SQLException cause = new SQLException("no connection");
    when(dataSource.getConnection()).thenThrow(cause).thenReturn(connection);

    MailServerException thrown =
        assertThrows(MailServerException.class, () -> transactionManager.execute(() -> "value"));

    assertEquals(ServerErrorCode.DATABASE_QUERY_FAILED, thrown.getErrorCode());
    assertSame(cause, thrown.getCause());
    Connection received = transactionManager.withConnection(connectionInUse -> connectionInUse);
    assertSame(connection, received);
  }

  @Test
  void rollsBackOnError() throws SQLException {
    AssertionError failure = new AssertionError("error");

    AssertionError thrown =
        assertThrows(
            AssertionError.class,
            () ->
                transactionManager.execute(
                    () -> {
                      throw failure;
                    }));

    assertSame(failure, thrown);
    verify(connection, times(1)).rollback();
  }

  @Test
  void nestedFailureRollsBackOnceByOuter() throws SQLException {
    IllegalStateException failure = new IllegalStateException();

    assertThrows(
        IllegalStateException.class,
        () ->
            transactionManager.execute(
                () ->
                    transactionManager.execute(
                        () -> {
                          throw failure;
                        })));

    verify(connection, times(1)).rollback();
    verify(connection, times(1)).close();
    verify(connection, never()).commit();
  }

  @Test
  void closeFailureAfterCommitDoesNotThrow() throws SQLException {
    org.mockito.Mockito.doThrow(new SQLException("close failed")).when(connection).close();

    String result = transactionManager.execute(() -> "done");

    assertEquals("done", result);
    verify(connection).commit();
  }

  @Test
  void executeWithoutResultCommits() throws SQLException {
    boolean[] ran = new boolean[1];

    transactionManager.executeWithoutResult(() -> ran[0] = true);

    assertEquals(true, ran[0]);
    verify(connection).commit();
  }
}
