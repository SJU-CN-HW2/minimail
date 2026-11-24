package com.chuseok22.minimail.server.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MailServerExceptionTest {

  @Test
  void messageIsFormattedWithArguments() {
    MailServerException exception =
        new MailServerException(ServerErrorCode.CONFIGURATION_VALUE_MISSING, "DB_HOST");

    assertEquals(ServerErrorCode.CONFIGURATION_VALUE_MISSING, exception.getErrorCode());
    assertEquals("DB_HOST 값이 비어 있습니다", exception.getMessage());
  }

  @Test
  void portOutOfRangeMessageContainsRange() {
    MailServerException exception =
        new MailServerException(
            ServerErrorCode.CONFIGURATION_PORT_OUT_OF_RANGE, "SMTP_PORT", 1, 65535);

    assertEquals("SMTP_PORT 값이 포트 범위(1~65535)를 벗어났습니다", exception.getMessage());
  }

  @Test
  void messageWithoutArgumentsIsErrorMessage() {
    MailServerException exception = new MailServerException(ServerErrorCode.INTERNAL_ERROR);

    assertEquals(ServerErrorCode.INTERNAL_ERROR.getErrorMessage(), exception.getMessage());
  }

  @Test
  void causeIsKept() {
    RuntimeException cause = new RuntimeException("원인");

    MailServerException exception =
        new MailServerException(ServerErrorCode.DATABASE_CONNECTION_FAILED, cause);

    assertSame(cause, exception.getCause());
    assertEquals("데이터베이스 커넥션 풀을 만들지 못했습니다", exception.getMessage());
  }

  @ParameterizedTest
  @EnumSource(
      names = {"DATABASE_CONNECTION_FAILED", "DATABASE_MIGRATION_FAILED", "INTERNAL_ERROR"})
  void causeConstructorCodesHaveNoPlaceholders(ServerErrorCode errorCode) {
    RuntimeException cause = new RuntimeException("원인");

    MailServerException exception = new MailServerException(errorCode, cause);

    assertSame(cause, exception.getCause());
    assertEquals(errorCode.getErrorMessage(), exception.getMessage());
  }
}
