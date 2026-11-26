package com.chuseok22.minimail.server.network;

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
  private final Supplier<ProtocolHandler> handlerFactory;
  private final Semaphore connectionPermits =
      new Semaphore(ServerConstants.MAXIMUM_CONCURRENT_CONNECTIONS);
  private ServerSocket serverSocket;

  public TcpServer(String protocolName, int port, Supplier<ProtocolHandler> handlerFactory) {
    this.protocolName = protocolName;
    this.port = port;
    this.handlerFactory = handlerFactory;
  }

  public void start() {
    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException exception) {
      log.error("{} protocol={} port={}",
          ServerErrorCode.LISTENER_OPEN_FAILED.getErrorMessage(), protocolName, port);
      throw new MailServerException(ServerErrorCode.LISTENER_OPEN_FAILED, exception);
    }
    log.info("리스너 시작. protocol={} port={}", protocolName, port);
    Thread.ofPlatform().name(protocolName + "-acceptor").start(this::acceptLoop);
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (IOException exception) {
      log.warn("{} protocol={}",
          ServerErrorCode.LISTENER_CLOSE_FAILED.getErrorMessage(), protocolName, exception);
    }
  }

  private void acceptLoop() {
    while (!serverSocket.isClosed()) {
      Socket socket;
      try {
        socket = serverSocket.accept();
      } catch (IOException exception) {
        if (!serverSocket.isClosed()) {
          log.error("{} protocol={}",
              ServerErrorCode.CONNECTION_ACCEPT_FAILED.getErrorMessage(), protocolName, exception);
        }
        continue;
      }
      if (!connectionPermits.tryAcquire()) {
        log.warn("{} protocol={} remote={} limit={}",
            ServerErrorCode.CONNECTION_LIMIT_EXCEEDED.getErrorMessage(), protocolName,
            socket.getRemoteSocketAddress(), ServerConstants.MAXIMUM_CONCURRENT_CONNECTIONS);
        closeQuietly(socket);
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
    MDC.put("sessionId", UUID.randomUUID().toString().substring(0, SESSION_ID_LENGTH));
    MDC.put("protocol", protocolName);
    MDC.put("remote", String.valueOf(socket.getRemoteSocketAddress()));
    ProtocolHandler handler = null;
    Session session = null;
    try {
      handler = handlerFactory.get();
      session = new Session(socket);
      log.info("연결 수립");
      handler.onConnect(session);
      while (!session.isClosed()) {
        String line = session.readLine();
        if (line == null) {
          break;
        }
        log.debug("C: {}", line);
        handler.onLine(session, line);
      }
    } catch (RuntimeException exception) {
      logSessionError(toMailServerException(exception), session);
    } finally {
      finish(handler, session, socket);
      MDC.clear();
    }
  }

  private void finish(ProtocolHandler handler, Session session, Socket socket) {
    if (session == null) {
      closeQuietly(socket);
      log.info("연결 종료");
      return;
    }
    try {
      handler.onDisconnect(session);
    } catch (RuntimeException exception) {
      log.error(ServerErrorCode.DISCONNECT_HANDLING_FAILED.getErrorMessage(), exception);
    }
    session.close();
    log.info("연결 종료");
  }

  private static void logSessionError(MailServerException exception, Session session) {
    ServerErrorCode errorCode = exception.getErrorCode();
    if (errorCode == ServerErrorCode.INTERNAL_ERROR) {
      log.error("연결을 끊습니다. errorCode={}", errorCode, exception);
      return;
    }
    if (session != null && session.isClosed()) {
      return;
    }
    log.warn("연결을 끊습니다. errorCode={} reason={}", errorCode, exception.getMessage());
  }

  private static MailServerException toMailServerException(RuntimeException exception) {
    if (exception instanceof MailServerException mailServerException) {
      return mailServerException;
    }
    return new MailServerException(ServerErrorCode.INTERNAL_ERROR, exception);
  }

  private static void closeQuietly(Socket socket) {
    try {
      socket.close();
    } catch (IOException exception) {
      log.warn(ServerErrorCode.SOCKET_CLOSE_FAILED.getErrorMessage(), exception);
    }
  }
}
