package com.chuseok22.minimail.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> ClientConfig.from(environment));

    assertTrue(exception.getMessage().contains(key));
  }

  @Test
  void blankHostFails() {
    Map<String, String> environment = validEnvironment();
    environment.put("MAIL_SERVER_HOST", "   ");

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> ClientConfig.from(environment));

    assertTrue(exception.getMessage().contains("MAIL_SERVER_HOST"));
  }

  @ParameterizedTest
  @CsvSource({
    "SMTP_PORT,0", "SMTP_PORT,65536", "SMTP_PORT,-1", "SMTP_PORT,abc",
    "POP3_PORT,0", "POP3_PORT,65536", "POP3_PORT,-1", "POP3_PORT,abc",
    "IMAP_PORT,0", "IMAP_PORT,65536", "IMAP_PORT,-1", "IMAP_PORT,abc"
  })
  void invalidPortFails(String key, String invalidPort) {
    Map<String, String> environment = validEnvironment();
    environment.put(key, invalidPort);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> ClientConfig.from(environment));

    assertTrue(exception.getMessage().contains(key));
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
