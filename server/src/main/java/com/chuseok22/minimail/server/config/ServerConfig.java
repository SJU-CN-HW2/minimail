package com.chuseok22.minimail.server.config;

import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerConstants;
import com.chuseok22.minimail.server.common.ServerErrorCode;
import java.util.Map;

/**
 * 서버 설정. 환경변수에서 읽고 검증한 값을 담는다.
 *
 * <p>검증은 {@link #from(Map)}에서만 한다. 실패 메시지에는 키 이름만 넣고 입력 값은 넣지 않는다.
 */
public record ServerConfig(
    String databaseHost,
    int databasePort,
    String databaseName,
    String databaseUser,
    String databasePassword,
    int smtpPort,
    int pop3Port,
    int imapPort) {

  private static final String DATABASE_HOST_KEY = "DB_HOST";
  private static final String DATABASE_PORT_KEY = "DB_PORT";
  private static final String DATABASE_NAME_KEY = "DB_NAME";
  private static final String DATABASE_USER_KEY = "DB_USER";
  private static final String DATABASE_PASSWORD_KEY = "DB_PASSWORD";
  private static final String SMTP_PORT_KEY = "SMTP_PORT";
  private static final String POP3_PORT_KEY = "POP3_PORT";
  private static final String IMAP_PORT_KEY = "IMAP_PORT";

  private static final int MINIMUM_PORT = 1;
  private static final int MAXIMUM_PORT = 65535;

  /** 실제 환경변수에서 설정을 읽는다. 서버 모듈에서 {@code System.getenv}를 호출하는 유일한 곳이다. */
  public static ServerConfig fromEnvironment() {
    return from(System.getenv());
  }

  /** 주어진 환경변수 맵을 검증해 설정을 만든다. 값이 없거나 형식이 틀리면 예외를 던진다. */
  public static ServerConfig from(Map<String, String> environment) {
    return new ServerConfig(
        readTrimmedText(environment, DATABASE_HOST_KEY),
        readPort(environment, DATABASE_PORT_KEY),
        readTrimmedText(environment, DATABASE_NAME_KEY),
        readTrimmedText(environment, DATABASE_USER_KEY),
        readPassword(environment, DATABASE_PASSWORD_KEY),
        readPort(environment, SMTP_PORT_KEY),
        readPort(environment, POP3_PORT_KEY),
        readPort(environment, IMAP_PORT_KEY));
  }

  private static String readTrimmedText(Map<String, String> environment, String key) {
    return readPresentValue(environment, key).trim();
  }

  /** 비밀번호는 앞뒤 공백을 보존하기 위해 원문 그대로 반환한다. */
  private static String readPassword(Map<String, String> environment, String key) {
    return readPresentValue(environment, key);
  }

  private static String readPresentValue(Map<String, String> environment, String key) {
    String value = environment.get(key);
    if (value == null || value.isBlank()) {
      throw new MailServerException(ServerErrorCode.CONFIGURATION_VALUE_MISSING, key);
    }
    return value;
  }

  private static int readPort(Map<String, String> environment, String key) {
    String text = readTrimmedText(environment, key);
    int port;
    try {
      port = Integer.parseInt(text);
    } catch (NumberFormatException exception) {
      throw new MailServerException(ServerErrorCode.CONFIGURATION_VALUE_NOT_INTEGER, key);
    }
    if (port < MINIMUM_PORT || port > MAXIMUM_PORT) {
      throw new MailServerException(
          ServerErrorCode.CONFIGURATION_PORT_OUT_OF_RANGE, key, MINIMUM_PORT, MAXIMUM_PORT);
    }
    return port;
  }

  @Override
  public String toString() {
    return "ServerConfig[databaseHost=" + databaseHost
        + ", databasePort=" + databasePort
        + ", databaseName=" + databaseName
        + ", databaseUser=" + databaseUser
        + ", databasePassword=" + ServerConstants.MASKED_PASSWORD
        + ", smtpPort=" + smtpPort
        + ", pop3Port=" + pop3Port
        + ", imapPort=" + imapPort + "]";
  }
}
