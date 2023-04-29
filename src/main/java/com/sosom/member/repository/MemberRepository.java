package com.sosom.member.repository;

import com.sosom.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findOptionalByEmail (String email);
    Optional<Member> findByNickname (String nickname);
    Member findByEmail (String email);
}
