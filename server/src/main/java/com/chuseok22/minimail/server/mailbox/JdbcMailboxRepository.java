package com.chuseok22.minimail.server.mailbox;

import com.chuseok22.minimail.server.database.TransactionManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/** JDBC로 구현한 메일함 저장소. */
@RequiredArgsConstructor
public final class JdbcMailboxRepository implements MailboxRepository {

  private static final String INBOX_NAME = "INBOX";

  private static final String ID_COLUMN = "id";
  private static final String MEMBER_ID_COLUMN = "member_id";
  private static final String NAME_COLUMN = "name";
  private static final String UID_VALIDITY_COLUMN = "uid_validity";
  private static final String NEXT_UID_COLUMN = "next_uid";

  private static final String SELECT_SQL =
      "SELECT id, member_id, name, uid_validity, next_uid FROM mailbox WHERE member_id = ?";
  private static final String FIND_BY_NAME_SQL = SELECT_SQL + " AND name = ?";
  private static final String FIND_ALL_SQL = SELECT_SQL + " ORDER BY name";

  private final TransactionManager transactionManager;

  @Override
  public Optional<Mailbox> findByMemberIdAndName(UUID memberId, String name) {
    String normalizedName = INBOX_NAME.equalsIgnoreCase(name) ? INBOX_NAME : name;
    return transactionManager.withConnection(
        connection -> {
          try (var statement = connection.prepareStatement(FIND_BY_NAME_SQL)) {
            statement.setObject(1, memberId);
            statement.setString(2, normalizedName);
            try (var resultSet = statement.executeQuery()) {
              return resultSet.next() ? Optional.of(toMailbox(resultSet)) : Optional.empty();
            }
          }
        });
  }

  @Override
  public List<Mailbox> findAllByMemberId(UUID memberId) {
    return transactionManager.withConnection(
        connection -> {
          try (var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            statement.setObject(1, memberId);
            try (var resultSet = statement.executeQuery()) {
              List<Mailbox> mailboxes = new ArrayList<>();
              while (resultSet.next()) {
                mailboxes.add(toMailbox(resultSet));
              }
              return mailboxes;
            }
          }
        });
  }

  private Mailbox toMailbox(ResultSet resultSet) throws SQLException {
    return new Mailbox(
        resultSet.getObject(ID_COLUMN, UUID.class),
        resultSet.getObject(MEMBER_ID_COLUMN, UUID.class),
        resultSet.getString(NAME_COLUMN),
        resultSet.getLong(UID_VALIDITY_COLUMN),
        resultSet.getLong(NEXT_UID_COLUMN));
  }
}
