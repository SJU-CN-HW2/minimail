package com.chuseok22.minimail.client.config;

import com.chuseok22.minimail.client.common.ClientErrorCode;
import com.chuseok22.minimail.client.common.MailClientException;
import java.util.Map;

/**
 * 클라이언트 설정. 환경변수에서 읽고 검증한 값을 담는다.
 *
 * <p>검증은 {@link #from(Map)}에서만 한다. 실패 메시지에는 키 이름만 넣고 입력 값은 넣지 않는다.
 */
public record ClientConfig(String mailServerHost, int smtpPort, int pop3Port, int imapPort) {

  private static final String MAIL_SERVER_HOST_KEY = "MAIL_SERVER_HOST";
  private static final String SMTP_PORT_KEY = "SMTP_PORT";
  private static final String POP3_PORT_KEY = "POP3_PORT";
  private static final String IMAP_PORT_KEY = "IMAP_PORT";

  private static final int MINIMUM_PORT = 1;
  private static final int MAXIMUM_PORT = 65535;

  /** 실제 환경변수에서 설정을 읽는다. 클라이언트 모듈에서 {@code System.getenv}를 호출하는 유일한 곳이다. */
  public static ClientConfig fromEnvironment() {
    return from(System.getenv());
  }

  /** 주어진 환경변수 맵을 검증해 설정을 만든다. 값이 없거나 형식이 틀리면 예외를 던진다. */
  public static ClientConfig from(Map<String, String> environment) {
    return new ClientConfig(
        readTrimmedText(environment, MAIL_SERVER_HOST_KEY),
        readPort(environment, SMTP_PORT_KEY),
        readPort(environment, POP3_PORT_KEY),
        readPort(environment, IMAP_PORT_KEY));
  }

  private static String readTrimmedText(Map<String, String> environment, String key) {
    String value = environment.get(key);
    if (value == null || value.isBlank()) {
      throw new MailClientException(ClientErrorCode.CONFIGURATION_VALUE_MISSING, key);
    }
    return value.trim();
  }

  private static int readPort(Map<String, String> environment, String key) {
    String text = readTrimmedText(environment, key);
    int port;
    try {
      port = Integer.parseInt(text);
    } catch (NumberFormatException exception) {
      throw new MailClientException(ClientErrorCode.CONFIGURATION_VALUE_NOT_INTEGER, key);
    }
    if (port < MINIMUM_PORT || port > MAXIMUM_PORT) {
      throw new MailClientException(
          ClientErrorCode.CONFIGURATION_PORT_OUT_OF_RANGE, key, MINIMUM_PORT, MAXIMUM_PORT);
    }
    return port;
  }
}
