package com.chuseok22.minimail.server.common;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ServerErrorCodeTest {

  @ParameterizedTest
  @EnumSource(ServerErrorCode.class)
  void everyErrorMessageIsNotBlank(ServerErrorCode errorCode) {
    assertFalse(errorCode.getErrorMessage().isBlank());
  }
}
