package com.chuseok22.minimail.server.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerConstants;
import com.chuseok22.minimail.server.common.ServerErrorCode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SessionTest {

  private final ByteArrayOutputStream sentBytes = new ByteArrayOutputStream();
  private Socket socket;

  private Session sessionReceiving(String received) throws IOException {
    return sessionReceiving(received.getBytes(StandardCharsets.UTF_8));
  }

  private Session sessionReceiving(byte[] received) throws IOException {
    socket = mock(Socket.class);
    when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(received));
    when(socket.getOutputStream()).thenReturn(sentBytes);
    return new Session(socket);
  }

  @Test
  void readLineSplitsLinesAndRemovesCarriageReturnLineFeed() throws IOException {
    Session session = sessionReceiving("USER alice\r\nPASS secret\r\n");

    assertEquals("USER alice", session.readLine());
    assertEquals("PASS secret", session.readLine());
    assertNull(session.readLine());
  }

  @Test
  void readLineAcceptsLineFeedOnly() throws IOException {
    Session session = sessionReceiving("NOOP\n");

    assertEquals("NOOP", session.readLine());
  }

  @Test
  void readLineKeepsEmptyLine() throws IOException {
    Session session = sessionReceiving("\r\n.\r\n");

    assertEquals("", session.readLine());
    assertEquals(".", session.readLine());
  }

  @Test
  void readLineReturnsLastLineWithoutTerminator() throws IOException {
    Session session = sessionReceiving("QUIT");

    assertEquals("QUIT", session.readLine());
    assertNull(session.readLine());
  }

  @Test
  void readLineDecodesUtf8() throws IOException {
    Session session = sessionReceiving("Subject: 안녕하세요\r\n");

    assertEquals("Subject: 안녕하세요", session.readLine());
  }

  @Test
  void readLineAcceptsLineOfMaximumLength() throws IOException {
    String longest = "a".repeat(ServerConstants.MAXIMUM_LINE_BYTES);
    Session session = sessionReceiving(longest + "\r\n");

    assertEquals(longest, session.readLine());
  }

  @Test
  void readLineRejectsLineOverMaximumLength() throws IOException {
    Session session = sessionReceiving("a".repeat(ServerConstants.MAXIMUM_LINE_BYTES + 2) + "\r\n");

    MailServerException exception = assertThrows(MailServerException.class, session::readLine);
    assertEquals(ServerErrorCode.COMMAND_LINE_TOO_LONG, exception.getErrorCode());
  }

  @Test
  void readBytesReadsExactLengthThenLinesContinue() throws IOException {
    Session session = sessionReceiving("hello world\r\nNEXT\r\n");

    assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), session.readBytes(5));
    assertEquals(" world", session.readLine());
    assertEquals("NEXT", session.readLine());
  }

  @Test
  void readBytesFailsWhenConnectionEndsEarly() throws IOException {
    Session session = sessionReceiving("abc");

    MailServerException exception =
        assertThrows(MailServerException.class, () -> session.readBytes(10));
    assertEquals(ServerErrorCode.CONNECTION_CLOSED_EARLY, exception.getErrorCode());
  }

  @Test
  void writeLineAppendsCarriageReturnLineFeed() throws IOException {
    Session session = sessionReceiving("");

    session.writeLine("+OK 준비 완료");

    assertEquals("+OK 준비 완료\r\n", sentBytes.toString(StandardCharsets.UTF_8));
  }

  @Test
  void closeClosesSocketOnlyOnce() throws IOException {
    Session session = sessionReceiving("");

    session.close();
    session.close();

    assertTrue(session.isClosed());
    verify(socket, times(1)).close();
  }
}
