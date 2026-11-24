package com.chuseok22.minimail.server.mailbox;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** 메일함 저장소. */
public interface MailboxRepository {

  /** 회원의 메일함을 이름으로 찾는다. 이름이 대소문자 무시로 {@code INBOX}면 {@code INBOX}로 찾는다. */
  Optional<Mailbox> findByMemberIdAndName(UUID memberId, String name);

  /** 회원의 모든 메일함을 이름 순으로 돌려준다. */
  List<Mailbox> findAllByMemberId(UUID memberId);
}
