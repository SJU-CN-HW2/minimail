package com.chuseok22.minimail.client;

import com.chuseok22.minimail.client.config.ClientConfig;

/** 클라이언트 진입점. 설정을 읽어 접속 대상을 안내한다. */
public final class ClientMain {

  private static final int FAILURE_EXIT_CODE = 1;

  private ClientMain() {}

  public static void main(String[] arguments) {
    ClientConfig config;
    try {
      config = ClientConfig.fromEnvironment();
    } catch (IllegalArgumentException exception) {
      System.err.println("클라이언트 설정이 올바르지 않습니다: " + exception.getMessage());
      System.exit(FAILURE_EXIT_CODE);
      return;
    }
    System.out.println("접속 대상 서버: " + config.mailServerHost());
    System.out.println("SMTP 포트: " + config.smtpPort());
    System.out.println("POP3 포트: " + config.pop3Port());
    System.out.println("IMAP 포트: " + config.imapPort());
  }
}
