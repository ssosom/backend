package com.sosom.voice.controller;

import com.sosom.response.dto.IdDto;
import com.sosom.voice.dto.SendVoiceResponseDto;
import com.sosom.voice.dto.VoiceRequestDto;
import com.sosom.voice.service.VoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.sosom.websocket.WebSokcetConst.EMAIL;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "voice",description = "대화에 관한 api")
public class VoiceController {

    private final VoiceService voiceService;

    @Operation(summary = "대화 저장 및 대화방 생성",description = "대화 파일 및 메시지를 받아 대화방을 만듭니다.")
    @PostMapping("/api/voices")
    public ResponseEntity<IdDto> saveVoice(@Valid @RequestBody VoiceRequestDto voiceRequestDto, @AuthenticationPrincipal UserDetails userDetail){
        return new ResponseEntity<>(voiceService.saveVoice(voiceRequestDto,userDetail.getUsername()), HttpStatus.CREATED);
    }

    @MessageMapping("/{roomId}")
    @SendTo("/room/{roomId}")
    public ResponseEntity<SendVoiceResponseDto> sendChat(@DestinationVariable Long roomId, @RequestBody VoiceRequestDto voiceRequestDto, @Header(EMAIL) String email) {
        return new ResponseEntity<>(voiceService.sendVoice(roomId,voiceRequestDto,email),HttpStatus.CREATED) ;
    }
}
