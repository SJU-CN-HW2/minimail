package com.chuseok22.minimail.server.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class MailServerExceptionTest {

  @Test
  void errorCodeAndMessageAreKept() {
    MailServerException exception =
        new MailServerException(ErrorCode.CONFIGURATION_INVALID, "DB_HOST 값이 비어 있습니다");

    assertEquals(ErrorCode.CONFIGURATION_INVALID, exception.getErrorCode());
    assertEquals("DB_HOST 값이 비어 있습니다", exception.getMessage());
  }

  @Test
  void causeIsKept() {
    RuntimeException cause = new RuntimeException("원인");

    MailServerException exception =
        new MailServerException(ErrorCode.DATABASE_FAILURE, "DB 접속 실패", cause);

    assertSame(cause, exception.getCause());
  }
}
