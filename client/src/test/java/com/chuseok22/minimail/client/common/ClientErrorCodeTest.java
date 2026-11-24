package com.chuseok22.minimail.client.common;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ClientErrorCodeTest {

  @ParameterizedTest
  @EnumSource(ClientErrorCode.class)
  void everyErrorMessageIsNotBlank(ClientErrorCode errorCode) {
    assertFalse(errorCode.getErrorMessage().isBlank());
  }
}
