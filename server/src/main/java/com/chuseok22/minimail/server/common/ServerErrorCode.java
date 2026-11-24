package com.chuseok22.minimail.server.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 서버 내부 오류의 종류와 오류 메시지 템플릿.
 *
 * <p>오류 문구는 이 enum의 {@code errorMessage}에만 둔다. 템플릿은 {@link String#format} 형식이며,
 * 채울 값은 {@link MailServerException} 생성 시 인자로 넘긴다.
 *
 * <p>프로토콜별 오류는 명세 확정 후 담당자가 추가하며, 각 프로토콜 세션 루프가 자기 응답 형식으로 번역한다.
 */
@Getter
@RequiredArgsConstructor
public enum ServerErrorCode {

  /** 필수 설정 값이 없거나 비어 있는 경우. 인자: 설정 키 이름. */
  CONFIGURATION_VALUE_MISSING("%s 값이 비어 있습니다"),

  /** 정수여야 하는 설정 값이 정수가 아닌 경우. 인자: 설정 키 이름. */
  CONFIGURATION_VALUE_NOT_INTEGER("%s 값이 정수가 아닙니다"),

  /** 포트 설정 값이 허용 범위를 벗어난 경우. 인자: 설정 키 이름, 최소 포트, 최대 포트. */
  CONFIGURATION_PORT_OUT_OF_RANGE("%s 값이 포트 범위(%d~%d)를 벗어났습니다"),

  /** 커넥션 풀을 만들지 못한 경우(DB 접속 실패). */
  DATABASE_CONNECTION_FAILED("데이터베이스 커넥션 풀을 만들지 못했습니다"),

  /** 마이그레이션 적용에 실패한 경우. */
  DATABASE_MIGRATION_FAILED("데이터베이스 마이그레이션에 실패했습니다"),

  LISTENER_OPEN_FAILED("프로토콜 리스너 포트를 열지 못했습니다"),

  COMMAND_LINE_TOO_LONG("명령 줄이 최대 길이(%d바이트)를 넘었습니다"),

  LISTENER_CLOSE_FAILED("프로토콜 리스너를 닫지 못했습니다"),

  CONNECTION_ACCEPT_FAILED("연결을 수락하지 못했습니다"),

  CONNECTION_LIMIT_EXCEEDED("동시 연결 수 제한을 넘어 연결을 거절했습니다"),

  CONNECTION_SETUP_FAILED("연결을 준비하지 못했습니다"),

  CONNECTION_READ_TIMEOUT("클라이언트가 제한 시간 동안 아무것도 보내지 않았습니다"),

  CONNECTION_IO_FAILED("연결에서 입출력 오류가 발생했습니다"),

  CONNECTION_CLOSED_EARLY("연결이 끊겨 %d바이트 중 %d바이트만 받았습니다"),

  SOCKET_CLOSE_FAILED("소켓을 닫지 못했습니다"),

  DISCONNECT_HANDLING_FAILED("연결 종료 처리 중 오류가 발생했습니다"),
  
  /** 데이터베이스 질의나 트랜잭션 처리에 실패한 경우. */
  DATABASE_QUERY_FAILED("데이터베이스 작업에 실패했습니다"),

  /** 요청한 메일함이 없는 경우. */
  MAILBOX_NOT_FOUND("메일함을 찾을 수 없습니다"),

  /** 요청한 메일이 없는 경우. */
  MAIL_NOT_FOUND("메일을 찾을 수 없습니다"),

  /** 그 외 서버 내부 오류. */
  INTERNAL_ERROR("예상하지 못한 서버 오류가 발생했습니다");

  private final String errorMessage;
}
