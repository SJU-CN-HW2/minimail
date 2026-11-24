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

  /** 그 외 서버 내부 오류. */
  INTERNAL_ERROR("예상하지 못한 서버 오류가 발생했습니다");

  private final String errorMessage;
}
