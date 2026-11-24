package com.chuseok22.minimail.server.member;

import com.chuseok22.minimail.server.common.ServerConstants;
import java.time.Instant;
import java.util.UUID;

/** 회원 계정. */
public record Member(UUID id, String address, String password, Instant createdAt) {

  @Override
  public String toString() {
    return "Member[id=" + id
        + ", address=" + address
        + ", password=" + ServerConstants.MASKED_PASSWORD
        + ", createdAt=" + createdAt + "]";
  }
}
