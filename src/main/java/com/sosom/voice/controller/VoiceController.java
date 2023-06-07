package com.sosom.voice.controller;

import com.sosom.response.dto.IdDto;
import com.sosom.voice.dto.SendVoiceResponseDto;
import com.sosom.voice.dto.VoiceRequestDto;
import com.sosom.voice.service.VoiceService;
import com.sosom.websocket.WebSokcetConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

import static com.sosom.websocket.WebSokcetConst.EMAIL;

@RestController
@RequiredArgsConstructor
@Slf4j
public class VoiceController {

    private final VoiceService voiceService;

    @PostMapping("/api/voices")
    public ResponseEntity<IdDto> saveVoice(@Valid @RequestBody VoiceRequestDto voiceRequestDto, @AuthenticationPrincipal UserDetails userDetail){
        return voiceService.saveVoice(voiceRequestDto,userDetail);
    }

    @MessageMapping("/{roomId}")
    @SendTo("/room/{roomId}")
    public ResponseEntity<SendVoiceResponseDto> sendChat(@DestinationVariable Long roomId, @RequestBody VoiceRequestDto voiceRequestDto, @Header(EMAIL) String email) {
        return voiceService.sendVoice(roomId,voiceRequestDto,email);
    }
}
