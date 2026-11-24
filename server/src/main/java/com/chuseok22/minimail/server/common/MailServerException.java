package com.chuseok22.minimail.server.common;

import java.util.Locale;
import lombok.Getter;

/** {@link ServerErrorCode}를 담아 던지는 서버 공통 unchecked 예외. */
public class MailServerException extends RuntimeException {

  @Getter private final ServerErrorCode errorCode;

  /**
   * 오류 코드의 메시지 템플릿을 인자로 채운 예외를 만든다. {@code null}을 인자로 넘기지 않는다. 원인 예외를
   * 함께 넘겨도 인자로 취급되어 버려지므로, 원인이 있으면 {@link #MailServerException(ServerErrorCode,
   * Throwable)}를 쓴다.
   */
  public MailServerException(ServerErrorCode errorCode, Object... messageArguments) {
    super(String.format(Locale.ROOT, errorCode.getErrorMessage(), messageArguments));
    this.errorCode = errorCode;
  }

  /** 원인 예외를 보존한다. 메시지는 인자 없이 채운 오류 코드의 메시지이므로 자리표시자가 없는 코드에만 쓴다. */
  public MailServerException(ServerErrorCode errorCode, Throwable cause) {
    super(String.format(Locale.ROOT, errorCode.getErrorMessage()), cause);
    this.errorCode = errorCode;
  }
}
