package com.chuseok22.minimail.server.mail;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** 메일 저장소. */
public interface MailRepository {

  /**
   * 메일함의 다음 UID를 발급해 메일을 저장한다. UID 발급과 삽입은 한 트랜잭션이다.
   *
   * @throws com.chuseok22.minimail.server.common.MailServerException 메일함이 없으면 {@code
   *     MAILBOX_NOT_FOUND}
   */
  Mail insert(UUID mailboxId, byte[] rawMessage);

  /**
   * 메일함의 모든 메일을 UID 오름차순으로 돌려준다. 본문(`rawMessage`)까지 모두 읽으므로 메일이 크거나 많으면 메모리를
   * 많이 쓴다. 본문 없는 요약 조회는 프로토콜 담당자 검토 후 필요하면 추가한다.
   */
  List<Mail> findAllByMailboxId(UUID mailboxId);

  /** 메일함에서 UID로 메일을 찾는다. */
  Optional<Mail> findByMailboxIdAndUid(UUID mailboxId, long uid);

  /**
   * 메일의 플래그를 바꾼다. 두 플래그를 한꺼번에 덮어쓰므로, 호출자가 읽어 둔 값과 쓰는 값 사이에 다른 세션이 바꾼 플래그는
   * 되돌려진다. 동시 변경이 문제가 되면 플래그별 메서드로 나누는 것을 프로토콜 담당자 검토 때 정한다.
   *
   * @throws com.chuseok22.minimail.server.common.MailServerException 메일이 없으면 {@code
   *     MAIL_NOT_FOUND}
   */
  void updateFlags(UUID mailId, boolean seen, boolean deleted);

  /**
   * 삭제 표시된 메일을 지우고 지운 UID를 오름차순으로 돌려준다. 없으면 빈 목록이다. POP3 `QUIT`와 IMAP `EXPUNGE`가
   * 함께 쓴다. POP3 `DELE` 표시는 세션 메모리에 두고 `QUIT`에서 한 번에 반영해야 한다(RFC 1939: `QUIT` 없이 끊기면 삭제하지
   * 않는다). DB의 `deleted` 플래그에 미리 쓰면 끊긴 세션의 표시가 남는다.
   */
  List<Long> deleteMarkedAsDeleted(UUID mailboxId);

  /**
   * 메일을 대상 메일함으로 복사한다. 본문, 크기, 수신 시각, 플래그를 보존하고 대상 메일함에서 새 UID를 받는다. 한 트랜잭션이다.
   *
   * @throws com.chuseok22.minimail.server.common.MailServerException 대상 메일함이 없으면 {@code
   *     MAILBOX_NOT_FOUND}, 원본이 없으면 {@code MAIL_NOT_FOUND}
   */
  Mail copy(UUID mailId, UUID targetMailboxId);
}
