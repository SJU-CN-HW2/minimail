package com.chuseok22.minimail.server.member;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chuseok22.minimail.server.common.ServerConstants;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MemberTest {

  @Test
  void toStringMasksPassword() {
    Member member = new Member(UUID.randomUUID(), "a@minimail", "secret-password", Instant.EPOCH);

    String text = member.toString();

    assertFalse(text.contains("secret-password"));
    assertTrue(text.contains(ServerConstants.MASKED_PASSWORD));
  }
}
