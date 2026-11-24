package com.chuseok22.minimail.server.mail;

import java.time.Instant;
import java.util.UUID;

/**
 * 메일함에 저장된 메일.
 *
 * <p>{@code byte[]}는 배열이라 {@code equals}가 내용을 비교하지 않는다. 본문을 비교하려면 {@link
 * java.util.Arrays#equals(byte[], byte[])}를 쓴다.
 */
public record Mail(
    UUID id,
    UUID mailboxId,
    long uid,
    byte[] rawMessage,
    long sizeOctets,
    Instant receivedAt,
    boolean seen,
    boolean deleted) {}
