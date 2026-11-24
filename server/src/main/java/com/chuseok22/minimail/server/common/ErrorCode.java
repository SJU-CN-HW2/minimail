package com.chuseok22.minimail.server.common;

/**
 * 서버 내부 오류의 종류.
 *
 * <p>프로토콜별 오류는 명세 확정 후 담당자가 추가하며, 각 프로토콜 세션 루프가 자기 응답 형식으로 번역한다.
 */
public enum ErrorCode {

  /** 시작 시 설정 검증에 실패한 경우. */
  CONFIGURATION_INVALID,

  /** DB 접속 또는 쿼리에 실패한 경우. */
  DATABASE_FAILURE,

  /** 그 외 서버 내부 오류. */
  INTERNAL_ERROR
}
