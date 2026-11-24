package com.chuseok22.minimail.server.mail;

import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerErrorCode;
import com.chuseok22.minimail.server.database.ResultSets;
import com.chuseok22.minimail.server.database.TransactionManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/** JDBC로 구현한 메일 저장소. */
@RequiredArgsConstructor
public final class JdbcMailRepository implements MailRepository {

  private static final String ID_COLUMN = "id";
  private static final String MAILBOX_ID_COLUMN = "mailbox_id";
  private static final String UID_COLUMN = "uid";
  private static final String RAW_MESSAGE_COLUMN = "raw_message";
  private static final String SIZE_OCTETS_COLUMN = "size_octets";
  private static final String RECEIVED_AT_COLUMN = "received_at";
  private static final String SEEN_COLUMN = "seen";
  private static final String DELETED_COLUMN = "deleted";

  private static final String MAIL_COLUMNS =
      "id, mailbox_id, uid, raw_message, size_octets, received_at, seen, deleted";

  private static final String ISSUE_UID_SQL =
      "UPDATE mailbox SET next_uid = next_uid + 1 WHERE id = ? RETURNING next_uid - 1";
  private static final String INSERT_SQL =
      "INSERT INTO mail (mailbox_id, uid, raw_message, size_octets) VALUES (?, ?, ?, ?)"
          + " RETURNING "
          + MAIL_COLUMNS;
  private static final String COPY_SQL =
      "INSERT INTO mail (mailbox_id, uid, raw_message, size_octets, received_at, seen, deleted)"
          + " SELECT ?, ?, raw_message, size_octets, received_at, seen, deleted"
          + " FROM mail WHERE id = ? RETURNING "
          + MAIL_COLUMNS;
  private static final String FIND_ALL_SQL =
      "SELECT " + MAIL_COLUMNS + " FROM mail WHERE mailbox_id = ? ORDER BY uid";
  private static final String FIND_BY_UID_SQL =
      "SELECT " + MAIL_COLUMNS + " FROM mail WHERE mailbox_id = ? AND uid = ?";
  private static final String UPDATE_FLAGS_SQL =
      "UPDATE mail SET seen = ?, deleted = ? WHERE id = ?";
  private static final String DELETE_MARKED_SQL =
      "WITH removed AS (DELETE FROM mail WHERE mailbox_id = ? AND deleted RETURNING uid)"
          + " SELECT uid FROM removed ORDER BY uid";

  private final TransactionManager transactionManager;

  @Override
  public Mail insert(UUID mailboxId, byte[] rawMessage) {
    return transactionManager.execute(
        () -> {
          long uid = issueUid(mailboxId);
          return transactionManager.withConnection(
              connection -> {
                try (var statement = connection.prepareStatement(INSERT_SQL)) {
                  statement.setObject(1, mailboxId);
                  statement.setLong(2, uid);
                  statement.setBytes(3, rawMessage);
                  statement.setLong(4, rawMessage.length);
                  return readInsertedMail(statement);
                }
              });
        });
  }

  @Override
  public List<Mail> findAllByMailboxId(UUID mailboxId) {
    return transactionManager.withConnection(
        connection -> {
          try (var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            statement.setObject(1, mailboxId);
            try (var resultSet = statement.executeQuery()) {
              List<Mail> mails = new ArrayList<>();
              while (resultSet.next()) {
                mails.add(toMail(resultSet));
              }
              return mails;
            }
          }
        });
  }

  @Override
  public Optional<Mail> findByMailboxIdAndUid(UUID mailboxId, long uid) {
    return transactionManager.withConnection(
        connection -> {
          try (var statement = connection.prepareStatement(FIND_BY_UID_SQL)) {
            statement.setObject(1, mailboxId);
            statement.setLong(2, uid);
            try (var resultSet = statement.executeQuery()) {
              return resultSet.next() ? Optional.of(toMail(resultSet)) : Optional.empty();
            }
          }
        });
  }

  @Override
  public void updateFlags(UUID mailId, boolean seen, boolean deleted) {
    int updatedRows =
        transactionManager.withConnection(
            connection -> {
              try (var statement = connection.prepareStatement(UPDATE_FLAGS_SQL)) {
                statement.setBoolean(1, seen);
                statement.setBoolean(2, deleted);
                statement.setObject(3, mailId);
                return statement.executeUpdate();
              }
            });
    if (updatedRows == 0) {
      throw new MailServerException(ServerErrorCode.MAIL_NOT_FOUND);
    }
  }

  @Override
  public List<Long> deleteMarkedAsDeleted(UUID mailboxId) {
    return transactionManager.withConnection(
        connection -> {
          try (var statement = connection.prepareStatement(DELETE_MARKED_SQL)) {
            statement.setObject(1, mailboxId);
            try (var resultSet = statement.executeQuery()) {
              List<Long> removedUids = new ArrayList<>();
              while (resultSet.next()) {
                removedUids.add(resultSet.getLong(UID_COLUMN));
              }
              return removedUids;
            }
          }
        });
  }

  @Override
  public Mail copy(UUID mailId, UUID targetMailboxId) {
    return transactionManager.execute(
        () -> {
          long uid = issueUid(targetMailboxId);
          return transactionManager.withConnection(
              connection -> {
                try (var statement = connection.prepareStatement(COPY_SQL)) {
                  statement.setObject(1, targetMailboxId);
                  statement.setLong(2, uid);
                  statement.setObject(3, mailId);
                  try (var resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                      // 원본이 없다. 예외로 트랜잭션이 롤백되어 발급한 UID도 원래대로 돌아간다(바깥 트랜잭션에서 예외를 삼키면 UID 공백이 남는다).
                      throw new MailServerException(ServerErrorCode.MAIL_NOT_FOUND);
                    }
                    return toMail(resultSet);
                  }
                }
              });
        });
  }

  /** 메일함의 다음 UID를 발급한다. 호출자가 트랜잭션 안에서 부른다(행 잠금이 커밋까지 유지된다). */
  private long issueUid(UUID mailboxId) {
    return transactionManager.withConnection(
        connection -> {
          try (var statement = connection.prepareStatement(ISSUE_UID_SQL)) {
            statement.setObject(1, mailboxId);
            try (var resultSet = statement.executeQuery()) {
              if (!resultSet.next()) {
                throw new MailServerException(ServerErrorCode.MAILBOX_NOT_FOUND);
              }
              return resultSet.getLong(1);
            }
          }
        });
  }

  private Mail readInsertedMail(PreparedStatement statement) throws SQLException {
    try (var resultSet = statement.executeQuery()) {
      resultSet.next();
      return toMail(resultSet);
    }
  }

  private Mail toMail(ResultSet resultSet) throws SQLException {
    return new Mail(
        resultSet.getObject(ID_COLUMN, UUID.class),
        resultSet.getObject(MAILBOX_ID_COLUMN, UUID.class),
        resultSet.getLong(UID_COLUMN),
        resultSet.getBytes(RAW_MESSAGE_COLUMN),
        resultSet.getLong(SIZE_OCTETS_COLUMN),
        ResultSets.getInstant(resultSet, RECEIVED_AT_COLUMN),
        resultSet.getBoolean(SEEN_COLUMN),
        resultSet.getBoolean(DELETED_COLUMN));
  }
}
