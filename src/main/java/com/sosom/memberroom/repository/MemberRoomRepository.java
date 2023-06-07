package com.sosom.memberroom.repository;

import com.sosom.memberroom.domain.MemberRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRoomRepository extends JpaRepository<MemberRoom,Long> {
}
