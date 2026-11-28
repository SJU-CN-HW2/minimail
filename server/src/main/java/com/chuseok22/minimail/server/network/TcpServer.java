package com.chuseok22.minimail.server.network;

import com.chuseok22.minimail.server.common.GlobalExceptionHandler;
import com.chuseok22.minimail.server.common.MailServerException;
import com.chuseok22.minimail.server.common.ServerConstants;
import com.chuseok22.minimail.server.common.ServerErrorCode;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class TcpServer {

  private static final int SESSION_ID_LENGTH = 8;

  private final String protocolName;
  private final int port;
  private final int readTimeoutMilliseconds;
  private final Supplier<ProtocolHandler> handlerFactory;
  private final Semaphore connectionPermits =
      new Semaphore(ServerConstants.MAXIMUM_CONCURRENT_CONNECTIONS);
  private ServerSocket serverSocket;

  public TcpServer(String protocolName, int port, int readTimeoutMilliseconds,
      Supplier<ProtocolHandler> handlerFactory) {
    this.protocolName = protocolName;
    this.port = port;
    this.readTimeoutMilliseconds = readTimeoutMilliseconds;
    this.handlerFactory = handlerFactory;
  }

  public void start() {
    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException exception) {
      throw new MailServerException(ServerErrorCode.TCP_CONNECTION_FAILED, exception);
    }
    log.info("리스너 시작. protocol={} port={} readTimeout={}ms",
        protocolName, port, readTimeoutMilliseconds);
    Thread.ofPlatform().name(protocolName + "-acceptor").start(this::acceptLoop);
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (IOException exception) {
      GlobalExceptionHandler.handle(
          new MailServerException(ServerErrorCode.TCP_CONNECTION_FAILED, exception));
    }
  }

  private void acceptLoop() {
    while (!serverSocket.isClosed()) {
      Socket socket;
      try {
        socket = serverSocket.accept();
      } catch (IOException exception) {
        if (!serverSocket.isClosed()) {
          GlobalExceptionHandler.handle(
              new MailServerException(ServerErrorCode.TCP_CONNECTION_FAILED, exception));
        }
        continue;
      }
      if (!connectionPermits.tryAcquire()) {
        log.info("{} protocol={} remote={} limit={}",
            ServerErrorCode.CONNECTION_LIMIT_EXCEEDED.getErrorMessage(), protocolName,
            socket.getRemoteSocketAddress(), ServerConstants.MAXIMUM_CONCURRENT_CONNECTIONS);
        closeSocket(socket);
        continue;
      }
      Thread.ofVirtual().start(() -> {
        try {
          serve(socket);
        } finally {
          connectionPermits.release();
        }
      });
    }
    log.info("리스너 종료. protocol={} port={}", protocolName, port);
  }

  private void serve(Socket socket) {
    ProtocolHandler handler = null;
    Session session = null;
    try {
      MDC.put("sessionId", UUID.randomUUID().toString().substring(0, SESSION_ID_LENGTH));
      MDC.put("protocol", protocolName);
      MDC.put("remote", String.valueOf(socket.getRemoteSocketAddress()));
      handler = handlerFactory.get();
      session = new Session(socket, readTimeoutMilliseconds);
      log.info("연결 수립");
      handler.onConnect(session);
      while (!session.isClosed()) {
        String line = session.readLine();
        if (line == null) {
          break;
        }
        log.info("C: {}", line);
        handler.onLine(session, line);
      }
    } catch (RuntimeException exception) {
      GlobalExceptionHandler.handle(exception);
    } finally {
      finish(handler, session, socket);
      MDC.clear();
    }
  }

  private void finish(ProtocolHandler handler, Session session, Socket socket) {
    if (session == null) {
      closeSocket(socket);
      log.info("연결 종료");
      return;
    }
    try {
      handler.onDisconnect(session);
    } catch (RuntimeException exception) {
      GlobalExceptionHandler.handle(
          new MailServerException(ServerErrorCode.TCP_CONNECTION_FAILED, exception));
    }
    session.close();
    log.info("연결 종료");
  }

  private static void closeSocket(Socket socket) {
    try {
      socket.close();
    } catch (IOException exception) {
      GlobalExceptionHandler.handle(
          new MailServerException(ServerErrorCode.TCP_CONNECTION_FAILED, exception));
    }
  }
}
