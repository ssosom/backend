package com.sosom.voice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sosom.exception.CustomException;
import com.sosom.exception.ErrorCode;
import com.sosom.member.domain.Member;
import com.sosom.member.repository.MemberRepository;
import com.sosom.memberroom.domain.MemberRoom;
import com.sosom.memberroom.repository.MemberRoomRepository;
import com.sosom.response.dto.IdDto;
import com.sosom.room.domain.Room;
import com.sosom.room.repository.RoomRepository;
import com.sosom.voice.domain.Voice;
import com.sosom.voice.dto.SendVoiceResponseDto;
import com.sosom.voice.dto.VoiceRequestDto;
import com.sosom.voice.file.Base64MultiPartFile;
import com.sosom.voice.repository.VoiceRepository;
import com.sosom.voiceroom.domain.VoiceRoom;
import com.sosom.voiceroom.repository.VoiceRoomRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class VoiceService {

    @Value("${s3.folderName}")
    private String folderName;
    @Value("${s3.bucketName}")
    private String bucketName;
    @Value("${s3.url}")
    private String url;
    private final VoiceRepository voiceRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final VoiceRoomRepository voiceRoomRepository;
    private final MemberRoomRepository memberRoomRepository;
    private final AmazonS3 s3Client;

    public ResponseEntity<IdDto> saveVoice(VoiceRequestDto voiceRequestDto, UserDetails userDetail) {
        Member member = validateMember(userDetail.getUsername());

        String fileName = voiceUpload(voiceRequestDto);

        Voice voice = saveVoice(voiceRequestDto, member, fileName);

        List<Member> memberList = memberRepository.findActiveMembers(member, PageRequest.ofSize(2));

        saveRoom(member, voice, memberList);

        updateMemberLastActive(member);

        return new ResponseEntity<>(new IdDto(voice.getId()), HttpStatus.CREATED);
    }

    public ResponseEntity<SendVoiceResponseDto> sendVoice(Long roomId, VoiceRequestDto voiceRequestDto, String email) {
        Member member = validateMember(email);

        Room room = validateRoom(roomId);

        validateMemberRoom(member,room);

        String fileName = voiceUpload(voiceRequestDto);

        Voice voice = saveVoice(voiceRequestDto,member,fileName);

        saveVoiceRoom(voice,room);

        updateMemberLastActive(member);

        SendVoiceResponseDto body = new SendVoiceResponseDto(voice.getUrl(),voice.getMessage());

        return new ResponseEntity<>(body,HttpStatus.CREATED);
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

    private String voiceUpload(VoiceRequestDto voiceRequestDto) {
        String fileName = folderName + UUID.randomUUID() + ".mp3";
        byte[] decodeBytes = Base64.decodeBase64(voiceRequestDto.getVoice());
        Base64MultiPartFile multiPartFile = new Base64MultiPartFile(decodeBytes);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multiPartFile.getSize());
        metadata.setContentType(multiPartFile.getContentType());

        try{
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,fileName,multiPartFile.getInputStream(),metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            s3Client.putObject(putObjectRequest);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FAIL_VOICE_UPLOAD);
        }

        return fileName;
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
