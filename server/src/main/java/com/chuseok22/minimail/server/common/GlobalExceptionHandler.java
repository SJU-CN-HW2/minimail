package com.chuseok22.minimail.server.common;

import java.io.IOException;
import java.net.SocketTimeoutException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalExceptionHandler {

  public static MailServerException toMailServerException(Throwable exception) {
    if (exception instanceof MailServerException mailServerException) {
      return mailServerException;
    }
    if (exception instanceof SocketTimeoutException) {
      return new MailServerException(ServerErrorCode.TCP_CONNECTION_TIMEOUT, exception);
    }
    if (exception instanceof IOException) {
      return new MailServerException(ServerErrorCode.TCP_CONNECTION_FAILED, exception);
    }
    return new MailServerException(ServerErrorCode.INTERNAL_ERROR, exception);
  }

  public static void handle(Throwable exception) {
    MailServerException mailServerException = toMailServerException(exception);
    log.error("errorCode={} reason={}", mailServerException.getErrorCode(),
        mailServerException.getMessage(), mailServerException);
  }
}
