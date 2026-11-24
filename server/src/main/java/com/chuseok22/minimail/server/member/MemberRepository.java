package com.chuseok22.minimail.server.member;

import java.util.Optional;

/** 회원 저장소. */
public interface MemberRepository {

  /** 주소(대소문자 무시)로 회원을 찾는다. */
  Optional<Member> findByAddress(String address);
}
