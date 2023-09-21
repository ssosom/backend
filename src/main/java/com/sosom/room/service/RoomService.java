package com.sosom.room.service;

import com.sosom.exception.CustomException;
import com.sosom.exception.ErrorCode;
import com.sosom.member.domain.Member;
import com.sosom.member.repository.MemberRepository;
import com.sosom.memberroom.repository.MemberRoomRepository;
import com.sosom.room.domain.Room;
import com.sosom.room.dto.GetRoomVoicesDto;
import com.sosom.room.repository.RoomRepository;
import com.sosom.voiceroom.domain.VoiceRoom;
import com.sosom.voiceroom.repository.VoiceRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final VoiceRoomRepository voiceRoomRepository;

    public Slice<GetRoomVoicesDto> getRoomVoices(Long roomId, Pageable pageable,UserDetails userDetail) {
        Room room = validateRoom(roomId);

        Member member = validateMember(userDetail.getUsername());

        Slice<VoiceRoom> voiceRoomResult = voiceRoomRepository.findByRoom(room,pageable);

        return voiceRoomResult.map(voiceRoom -> new GetRoomVoicesDto(voiceRoom,member));
    }

    private Member validateMember(String email) {
        return memberRepository.findOptionalByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.FAIL_AUTHORIZATION));
    }
    private Room validateRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_ROOM));
    }

}
