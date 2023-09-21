package com.sosom.member.repository;

import com.sosom.member.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findOptionalByEmail (String email);
    Optional<Member> findOptionalByNickname (String nickname);
    Member findByEmail (String email);
    @Query("select m from Member m where m <> :member order by m.lastActive desc ")
    List<Member> findActiveMembers(@Param("member") Member member, Pageable pageable);
}
