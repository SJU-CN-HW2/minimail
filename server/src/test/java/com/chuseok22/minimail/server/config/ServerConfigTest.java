package com.chuseok22.minimail.server.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chuseok22.minimail.server.common.ErrorCode;
import com.chuseok22.minimail.server.common.MailServerException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ServerConfigTest {

  private static Map<String, String> validEnvironment() {
    Map<String, String> environment = new HashMap<>();
    environment.put("DB_HOST", "localhost");
    environment.put("DB_PORT", "5432");
    environment.put("DB_NAME", "minimail");
    environment.put("DB_USER", "minimail");
    environment.put("DB_PASSWORD", "secret-password");
    environment.put("SMTP_PORT", "2525");
    environment.put("POP3_PORT", "1110");
    environment.put("IMAP_PORT", "1143");
    return environment;
  }

  @Test
  void validEnvironmentIsLoaded() {
    ServerConfig config = ServerConfig.from(validEnvironment());

    assertEquals("localhost", config.databaseHost());
    assertEquals(5432, config.databasePort());
    assertEquals("minimail", config.databaseName());
    assertEquals("minimail", config.databaseUser());
    assertEquals("secret-password", config.databasePassword());
    assertEquals(2525, config.smtpPort());
    assertEquals(1110, config.pop3Port());
    assertEquals(1143, config.imapPort());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "DB_HOST", "DB_PORT", "DB_NAME", "DB_USER", "DB_PASSWORD", "SMTP_PORT", "POP3_PORT",
        "IMAP_PORT"
      })
  void missingKeyFails(String key) {
    Map<String, String> environment = validEnvironment();
    environment.remove(key);

    MailServerException exception =
        assertThrows(MailServerException.class, () -> ServerConfig.from(environment));

    assertEquals(ErrorCode.CONFIGURATION_INVALID, exception.getErrorCode());
    assertTrue(exception.getMessage().contains(key));
  }

  @Test
  void blankPasswordFails() {
    Map<String, String> environment = validEnvironment();
    environment.put("DB_PASSWORD", "  ");

    MailServerException exception =
        assertThrows(MailServerException.class, () -> ServerConfig.from(environment));

    assertEquals(ErrorCode.CONFIGURATION_INVALID, exception.getErrorCode());
    assertTrue(exception.getMessage().contains("DB_PASSWORD"));
  }

  @ParameterizedTest
  @CsvSource({
    "DB_PORT,0", "DB_PORT,65536", "DB_PORT,-1", "DB_PORT,abc",
    "SMTP_PORT,0", "SMTP_PORT,65536", "SMTP_PORT,-1", "SMTP_PORT,abc",
    "POP3_PORT,0", "POP3_PORT,65536", "POP3_PORT,-1", "POP3_PORT,abc",
    "IMAP_PORT,0", "IMAP_PORT,65536", "IMAP_PORT,-1", "IMAP_PORT,abc"
  })
  void invalidPortFails(String key, String invalidPort) {
    Map<String, String> environment = validEnvironment();
    environment.put(key, invalidPort);

    MailServerException exception =
        assertThrows(MailServerException.class, () -> ServerConfig.from(environment));

    assertEquals(ErrorCode.CONFIGURATION_INVALID, exception.getErrorCode());
    assertTrue(exception.getMessage().contains(key));
  }

  @Test
  void valuesAreTrimmed() {
    Map<String, String> environment = validEnvironment();
    environment.put("SMTP_PORT", " 2525 ");
    environment.put("DB_HOST", " localhost ");

    ServerConfig config = ServerConfig.from(environment);

    assertEquals(2525, config.smtpPort());
    assertEquals("localhost", config.databaseHost());
  }

  @Test
  void passwordIsNotTrimmed() {
    Map<String, String> environment = validEnvironment();
    environment.put("DB_PASSWORD", " secret ");

    ServerConfig config = ServerConfig.from(environment);

    assertEquals(" secret ", config.databasePassword());
  }

  @Test
  void failureMessageDoesNotContainValue() {
    Map<String, String> invalidPortEnvironment = validEnvironment();
    invalidPortEnvironment.put("SMTP_PORT", "not-a-port-value");
    Map<String, String> blankHostEnvironment = validEnvironment();
    blankHostEnvironment.put("DB_HOST", "   ");

    MailServerException invalidPort =
        assertThrows(MailServerException.class, () -> ServerConfig.from(invalidPortEnvironment));
    MailServerException blankHost =
        assertThrows(MailServerException.class, () -> ServerConfig.from(blankHostEnvironment));

    assertFalse(invalidPort.getMessage().contains("not-a-port-value"));
    assertEquals("DB_HOST 값이 비어 있습니다", blankHost.getMessage());
  }

  @Test
  void toStringMasksPassword() {
    ServerConfig config = ServerConfig.from(validEnvironment());

    String text = config.toString();

    assertFalse(text.contains("secret-password"));
    assertTrue(text.contains("****"));
  }
}
