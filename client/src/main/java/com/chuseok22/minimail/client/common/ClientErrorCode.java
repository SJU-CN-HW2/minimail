package com.chuseok22.minimail.client.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 클라이언트 오류의 종류와 오류 메시지 템플릿.
 *
 * <p>오류 문구는 이 enum의 {@code errorMessage}에만 둔다. 템플릿은 {@link String#format} 형식이며,
 * 채울 값은 {@link MailClientException} 생성 시 인자로 넘긴다.
 */
@Getter
@RequiredArgsConstructor
public enum ClientErrorCode {

  /** 필수 설정 값이 없거나 비어 있는 경우. 인자: 설정 키 이름. */
  CONFIGURATION_VALUE_MISSING("%s 값이 비어 있습니다"),

  /** 정수여야 하는 설정 값이 정수가 아닌 경우. 인자: 설정 키 이름. */
  CONFIGURATION_VALUE_NOT_INTEGER("%s 값이 정수가 아닙니다"),

  /** 포트 설정 값이 허용 범위를 벗어난 경우. 인자: 설정 키 이름, 최소 포트, 최대 포트. */
  CONFIGURATION_PORT_OUT_OF_RANGE("%s 값이 포트 범위(%d~%d)를 벗어났습니다");

  private final String errorMessage;
}
