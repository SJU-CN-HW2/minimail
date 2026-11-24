package com.chuseok22.minimail.client.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MailClientExceptionTest {

  @Test
  void messageIsFormattedWithArguments() {
    MailClientException exception =
        new MailClientException(ClientErrorCode.CONFIGURATION_VALUE_MISSING, "MAIL_SERVER_HOST");

    assertEquals(ClientErrorCode.CONFIGURATION_VALUE_MISSING, exception.getErrorCode());
    assertEquals("MAIL_SERVER_HOST 값이 비어 있습니다", exception.getMessage());
  }

  @Test
  void portOutOfRangeMessageContainsRange() {
    MailClientException exception =
        new MailClientException(
            ClientErrorCode.CONFIGURATION_PORT_OUT_OF_RANGE, "SMTP_PORT", 1, 65535);

    assertEquals("SMTP_PORT 값이 포트 범위(1~65535)를 벗어났습니다", exception.getMessage());
  }
}
