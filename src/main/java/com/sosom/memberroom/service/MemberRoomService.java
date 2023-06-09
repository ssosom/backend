package com.sosom.memberroom.service;

import com.sosom.exception.CustomException;
import com.sosom.exception.ErrorCode;
import com.sosom.member.domain.Member;
import com.sosom.member.repository.MemberRepository;
import com.sosom.memberroom.domain.MemberRoom;
import com.sosom.memberroom.dto.GetRoomsDto;
import com.sosom.memberroom.dto.updateMemberRoomNameDto;
import com.sosom.memberroom.repository.MemberRoomRepository;
import com.sosom.response.Result;
import com.sosom.room.domain.Room;
import com.sosom.voice.domain.Voice;
import com.sosom.voice.dto.SendVoiceResponseDto;
import com.sosom.voice.repository.VoiceRepository;
import com.sosom.voiceroom.domain.VoiceRoom;
import com.sosom.voiceroom.repository.VoiceRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberRoomService {

    public static final String LEAVE_MESSAGE = "대화방에서 나갔습니다.";
    @Value("${s3.aiUrl}")
    private String aiUrl;
    private final MemberRoomRepository memberRoomRepository;
    private final MemberRepository memberRepository;
    private final VoiceRepository voiceRepository;
    private final VoiceRoomRepository voiceRoomRepository;
    private final SimpMessagingTemplate template;

    public Result<List<GetRoomsDto>> getRooms(UserDetails userDetail) {
        Member member = validateMember(userDetail.getUsername());

        return new Result<>(memberRoomRepository.findByMember(member)) ;
    }
    public ResponseEntity<Void> updateMemberRoomName(Long memberRoomId, updateMemberRoomNameDto updateMemberRoomNameDto,String email) {
        MemberRoom memberRoom = validateMemberRoom(memberRoomId);

        Member member = validateMember(email);

        validateAuthority(memberRoom, member);

        memberRoom.updateMemberRoomName(updateMemberRoomNameDto.getRoomName());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Void> leaveMemberRoom(Long memberRoomId, String email) {
        MemberRoom memberRoom = validateMemberRoom(memberRoomId);

        Member member = validateMember(email);

        validateAuthority(memberRoom, member);

        memberRoomRepository.delete(memberRoom);

        saveLeaveAiVoice(memberRoom, member);

        template.convertAndSend("/room/"+memberRoom.getRoom().getId(),new SendVoiceResponseDto(aiUrl, LEAVE_MESSAGE));

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private void saveLeaveAiVoice(MemberRoom memberRoom, Member member) {
        Voice voice = Voice.createVoice(member,aiUrl,LEAVE_MESSAGE);
        voiceRepository.save(voice);

        Room room = memberRoom.getRoom();
        VoiceRoom voiceRoom = VoiceRoom.createVoiceRoom(voice,room);

        voiceRoomRepository.save(voiceRoom);
    }


    private MemberRoom validateMemberRoom(Long memberRoomId) {
        return memberRoomRepository.findByIdFetch(memberRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_MEMBER_ROOM));
    }

    private Member validateMember(String email) {
        return memberRepository.findOptionalByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.FAIL_AUTHORIZATION));
    }

    private void validateAuthority(MemberRoom memberRoom, Member member) {
        if (!memberRoom.getMember().equals(member)) {
            throw new CustomException(ErrorCode.FAIL_AUTHORITY);
        }
    }

}
