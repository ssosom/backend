package com.sosom.voice.service;

import com.sosom.exception.CustomException;
import com.sosom.exception.ErrorCode;
import com.sosom.member.domain.Member;
import com.sosom.member.repository.MemberRepository;
import com.sosom.memberroom.domain.MemberRoom;
import com.sosom.memberroom.repository.MemberRoomRepository;
import com.sosom.response.dto.IdDto;
import com.sosom.room.domain.Room;
import com.sosom.room.repository.RoomRepository;
import com.sosom.s3.service.S3Service;
import com.sosom.voice.domain.Voice;
import com.sosom.voice.dto.SendVoiceResponseDto;
import com.sosom.voice.dto.VoiceRequestDto;
import com.sosom.voice.repository.VoiceRepository;
import com.sosom.voiceroom.domain.VoiceRoom;
import com.sosom.voiceroom.repository.VoiceRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VoiceService {

    @Value("${s3.url}")
    private String url;
    private final VoiceRepository voiceRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final VoiceRoomRepository voiceRoomRepository;
    private final MemberRoomRepository memberRoomRepository;
    private final S3Service s3Service;


    public IdDto saveVoice(VoiceRequestDto voiceRequestDto, String email) {
        Member member = validateMember(email);

        String fileName = s3Service.base64Upload(voiceRequestDto.getVoice(),voiceRequestDto.getType());

        Voice voice = saveVoice(voiceRequestDto, member, fileName);

        List<Member> memberList = memberRepository.findActiveMembers(member, PageRequest.ofSize(2));

        saveRoom(member, voice, memberList);

        updateMemberLastActive(member);

        return new IdDto(voice.getId());
    }

    public SendVoiceResponseDto sendVoice(Long roomId, VoiceRequestDto voiceRequestDto, String email) {
        Member member = validateMember(email);

        Room room = validateRoom(roomId);

        validateMemberRoom(member,room);

        String fileName = s3Service.base64Upload(voiceRequestDto.getVoice(),voiceRequestDto.getType());

        Voice voice = saveVoice(voiceRequestDto,member,fileName);

        saveVoiceRoom(voice,room);

        updateMemberLastActive(member);

        return new SendVoiceResponseDto(voice.getUrl(),voice.getMessage());
    }

    private Member validateMember(String email) {
        return memberRepository.findOptionalByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.FAIL_AUTHORIZATION));
    }

    private Room validateRoom(Long roomId){
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_ROOM));
    }

    private void validateMemberRoom(Member member, Room room) {
        memberRoomRepository.findByMemberAndRoom(member,room)
                .orElseThrow(() -> new CustomException(ErrorCode.FAIL_AUTHORITY));
    }


    private Voice saveVoice(VoiceRequestDto voiceRequestDto, Member member, String fileName) {
        Voice voice = Voice.createVoice(member,url + fileName, voiceRequestDto.getMessage());
        voiceRepository.save(voice);
        return voice;
    }

    private void saveRoom(Member member, Voice voice, List<Member> memberList) {
        for (Member findMember : memberList) {

            Room room = saveRoom(member);

            saveVoiceRoom(voice, room);

            saveMemberRoom(member, room);

            saveMemberRoom(findMember, room);
        }
    }

    private Room saveRoom(Member member) {
        Room room = Room.createRoom(member);
        roomRepository.save(room);
        return room;
    }

    private void saveMemberRoom(Member member, Room room) {
        MemberRoom memberRoom = MemberRoom.createMemberRoom(member, room);
        memberRoomRepository.save(memberRoom);
    }

    private void saveVoiceRoom(Voice voice, Room room) {
        VoiceRoom voiceRoom = VoiceRoom.createVoiceRoom(voice, room);
        voiceRoomRepository.save(voiceRoom);
    }

    private void updateMemberLastActive(Member member) {
        member.updateLastActive(LocalDateTime.now());
    }
}
