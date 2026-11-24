package com.chuseok22.minimail.server.network;

public interface ProtocolHandler {
  void onConnect(Session session);
  void onLine(Session session, String line);
  default void onDisconnect(Session session) {}
}
