package com.chuseok22.minimail.server.network;

import com.chuseok22.minimail.server.common.GlobalExceptionHandler;
import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerConstants;
import com.chuseok22.minimail.server.common.ServerErrorCode;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Session {

  private static final String LINE_TERMINATOR = "\r\n";

  private final Socket socket;
  private final InputStream inputStream;
  private final OutputStream outputStream;
  private volatile boolean closed;

  public Session(Socket socket, int readTimeoutMilliseconds) {
    this.socket = socket;
    try {
      socket.setSoTimeout(readTimeoutMilliseconds);
      this.inputStream = new BufferedInputStream(socket.getInputStream());
      this.outputStream = socket.getOutputStream();
    } catch (IOException exception) {
      throw new MailServerException(ServerErrorCode.TCP_CONNECTION_FAILED, exception);
    }
  }

  public String readLine() {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int readByte;
    while ((readByte = readOneByte()) != -1) {
      if (readByte == '\n') {
        return decodeWithoutCarriageReturn(buffer.toByteArray());
      }
      buffer.write(readByte);
      if (buffer.size() > ServerConstants.MAXIMUM_LINE_BYTES + LINE_TERMINATOR.length()) {
        throw new MailServerException(ServerErrorCode.TCP_CONNECTION_FAILED);
      }
    }
    return buffer.size() > 0 ? decodeWithoutCarriageReturn(buffer.toByteArray()) : null;
  }

  public byte[] readBytes(int length) {
    byte[] data;
    try {
      data = inputStream.readNBytes(length);
    } catch (IOException exception) {
      throw GlobalExceptionHandler.toMailServerException(exception);
    }
    if (data.length < length) {
      throw new MailServerException(ServerErrorCode.TCP_CONNECTION_FAILED);
    }
    return data;
  }

  public void writeLine(String line) {
    log.info("S: {}", line);
    try {
      outputStream.write((line + LINE_TERMINATOR).getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
    } catch (IOException exception) {
      throw GlobalExceptionHandler.toMailServerException(exception);
    }
  }

  public void close() {
    if (closed) {
      return;
    }
    closed = true;
    try {
      socket.close();
    } catch (IOException exception) {
      GlobalExceptionHandler.handle(
          new MailServerException(ServerErrorCode.TCP_CONNECTION_FAILED, exception));
    }
  }

  public boolean isClosed() {
    return closed;
  }

  public String remoteAddress() {
    return String.valueOf(socket.getRemoteSocketAddress());
  }

  private int readOneByte() {
    try {
      return inputStream.read();
    } catch (IOException exception) {
      throw GlobalExceptionHandler.toMailServerException(exception);
    }
  }

  private static String decodeWithoutCarriageReturn(byte[] bytes) {
    int length = bytes.length;
    if (length > 0 && bytes[length - 1] == '\r') {
      length--;
    }
    return new String(bytes, 0, length, StandardCharsets.UTF_8);
  }
}
