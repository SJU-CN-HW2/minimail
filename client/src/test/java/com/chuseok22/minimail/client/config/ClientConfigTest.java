package com.chuseok22.minimail.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chuseok22.minimail.client.common.ClientErrorCode;
import com.chuseok22.minimail.client.common.MailClientException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ClientConfigTest {

  private static Map<String, String> validEnvironment() {
    Map<String, String> environment = new HashMap<>();
    environment.put("MAIL_SERVER_HOST", "localhost");
    environment.put("SMTP_PORT", "2525");
    environment.put("POP3_PORT", "1110");
    environment.put("IMAP_PORT", "1143");
    return environment;
  }

  @Test
  void validEnvironmentIsLoaded() {
    ClientConfig config = ClientConfig.from(validEnvironment());

    assertEquals("localhost", config.mailServerHost());
    assertEquals(2525, config.smtpPort());
    assertEquals(1110, config.pop3Port());
    assertEquals(1143, config.imapPort());
  }

  @ParameterizedTest
  @ValueSource(strings = {"MAIL_SERVER_HOST", "SMTP_PORT", "POP3_PORT", "IMAP_PORT"})
  void missingKeyFails(String key) {
    Map<String, String> environment = validEnvironment();
    environment.remove(key);

    MailClientException exception =
        assertThrows(MailClientException.class, () -> ClientConfig.from(environment));

    assertEquals(ClientErrorCode.CONFIGURATION_VALUE_MISSING, exception.getErrorCode());
    assertTrue(exception.getMessage().contains(key));
  }

  @Test
  void blankHostFails() {
    Map<String, String> environment = validEnvironment();
    environment.put("MAIL_SERVER_HOST", "   ");

    MailClientException exception =
        assertThrows(MailClientException.class, () -> ClientConfig.from(environment));

    assertEquals(ClientErrorCode.CONFIGURATION_VALUE_MISSING, exception.getErrorCode());
    assertTrue(exception.getMessage().contains("MAIL_SERVER_HOST"));
  }

  @ParameterizedTest
  @CsvSource({
    "SMTP_PORT,0,CONFIGURATION_PORT_OUT_OF_RANGE",
    "SMTP_PORT,65536,CONFIGURATION_PORT_OUT_OF_RANGE",
    "SMTP_PORT,-1,CONFIGURATION_PORT_OUT_OF_RANGE",
    "SMTP_PORT,abc,CONFIGURATION_VALUE_NOT_INTEGER",
    "POP3_PORT,0,CONFIGURATION_PORT_OUT_OF_RANGE",
    "POP3_PORT,65536,CONFIGURATION_PORT_OUT_OF_RANGE",
    "POP3_PORT,-1,CONFIGURATION_PORT_OUT_OF_RANGE",
    "POP3_PORT,abc,CONFIGURATION_VALUE_NOT_INTEGER",
    "IMAP_PORT,0,CONFIGURATION_PORT_OUT_OF_RANGE",
    "IMAP_PORT,65536,CONFIGURATION_PORT_OUT_OF_RANGE",
    "IMAP_PORT,-1,CONFIGURATION_PORT_OUT_OF_RANGE",
    "IMAP_PORT,abc,CONFIGURATION_VALUE_NOT_INTEGER"
  })
  void invalidPortFails(String key, String invalidPort, ClientErrorCode expectedErrorCode) {
    Map<String, String> environment = validEnvironment();
    environment.put(key, invalidPort);

    MailClientException exception =
        assertThrows(MailClientException.class, () -> ClientConfig.from(environment));

    assertEquals(expectedErrorCode, exception.getErrorCode());
    assertEquals(expectedMessage(expectedErrorCode, key), exception.getMessage());
  }

  private static String expectedMessage(ClientErrorCode errorCode, String key) {
    return switch (errorCode) {
      case CONFIGURATION_PORT_OUT_OF_RANGE -> key + " 값이 포트 범위(1~65535)를 벗어났습니다";
      case CONFIGURATION_VALUE_NOT_INTEGER -> key + " 값이 정수가 아닙니다";
      default -> throw new IllegalArgumentException(errorCode.name());
    };
  }

  @Test
  void valuesAreTrimmed() {
    Map<String, String> environment = validEnvironment();
    environment.put("SMTP_PORT", " 2525 ");
    environment.put("MAIL_SERVER_HOST", " localhost ");

    ClientConfig config = ClientConfig.from(environment);

    assertEquals(2525, config.smtpPort());
    assertEquals("localhost", config.mailServerHost());
  }
}
