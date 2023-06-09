package com.sosom.memberroom.repository;

import com.sosom.member.domain.Member;
import com.sosom.memberroom.dto.GetRoomsDto;

import java.util.List;

public interface MemberRoomRepositoryCustom {
    List<GetRoomsDto> findByMember(Member findMember);
}
