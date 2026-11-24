package com.chuseok22.minimail.server.common;

/** {@link ErrorCode}를 담아 던지는 서버 공통 unchecked 예외. */
public class MailServerException extends RuntimeException {

  private final ErrorCode errorCode;

  public MailServerException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public MailServerException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
