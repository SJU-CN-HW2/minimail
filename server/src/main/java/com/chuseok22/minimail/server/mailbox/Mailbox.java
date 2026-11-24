package com.chuseok22.minimail.server.mailbox;

import java.util.UUID;

/** 회원의 메일함. */
public record Mailbox(UUID id, UUID memberId, String name, long uidValidity, long nextUid) {}
