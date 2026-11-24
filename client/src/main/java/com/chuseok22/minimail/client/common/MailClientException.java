package com.chuseok22.minimail.client.common;

import java.util.Locale;

/** {@link ClientErrorCode}를 담아 던지는 클라이언트 공통 unchecked 예외. */
public class MailClientException extends RuntimeException {

  private final ClientErrorCode errorCode;

  /** 오류 코드의 메시지 템플릿을 인자로 채운 예외를 만든다. {@code null}을 인자로 넘기지 않는다. */
  public MailClientException(ClientErrorCode errorCode, Object... messageArguments) {
    super(String.format(Locale.ROOT, errorCode.getErrorMessage(), messageArguments));
    this.errorCode = errorCode;
  }

  public ClientErrorCode getErrorCode() {
    return errorCode;
  }
}
