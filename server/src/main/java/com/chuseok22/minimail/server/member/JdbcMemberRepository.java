package com.chuseok22.minimail.server.member;

import com.chuseok22.minimail.server.database.ResultSets;
import com.chuseok22.minimail.server.database.TransactionManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/** JDBC로 구현한 회원 저장소. */
@RequiredArgsConstructor
public final class JdbcMemberRepository implements MemberRepository {

  private static final String ID_COLUMN = "id";
  private static final String ADDRESS_COLUMN = "address";
  private static final String PASSWORD_COLUMN = "password";
  private static final String CREATED_AT_COLUMN = "created_at";

  private static final String FIND_BY_ADDRESS_SQL =
      "SELECT id, address, password, created_at FROM member WHERE address = ?";

  private final TransactionManager transactionManager;

  @Override
  public Optional<Member> findByAddress(String address) {
    return transactionManager.withConnection(
        connection -> {
          try (var statement = connection.prepareStatement(FIND_BY_ADDRESS_SQL)) {
            statement.setString(1, address.toLowerCase(Locale.ROOT));
            try (var resultSet = statement.executeQuery()) {
              return resultSet.next() ? Optional.of(toMember(resultSet)) : Optional.empty();
            }
          }
        });
  }

  private Member toMember(ResultSet resultSet) throws SQLException {
    return new Member(
        resultSet.getObject(ID_COLUMN, UUID.class),
        resultSet.getString(ADDRESS_COLUMN),
        resultSet.getString(PASSWORD_COLUMN),
        ResultSets.getInstant(resultSet, CREATED_AT_COLUMN));
  }
}
