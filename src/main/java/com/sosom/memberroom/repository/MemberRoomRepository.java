package com.sosom.memberroom.repository;

import com.sosom.member.domain.Member;
import com.sosom.memberroom.domain.MemberRoom;
import com.sosom.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRoomRepository extends JpaRepository<MemberRoom,Long>,MemberRoomRepositoryCustom {
    Optional<MemberRoom> findByMemberAndRoom(Member member, Room room);
    @Query("select mr from MemberRoom as mr join fetch mr.member join fetch mr.room where mr.id = :memberRoomId")
    Optional<MemberRoom> findByIdFetch(@Param("memberRoomId") Long memberRoomId);
}
