package com.sosom.room.controller;

import com.sosom.room.dto.GetRoomVoicesDto;
import com.sosom.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "room",description = "대화방에 관한 api")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "대화방의 대화 목록",description = "대화방의 아이디와 page,size를 받아 페이징한 대화 목록을 반환합니다")
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<Slice<GetRoomVoicesDto>> getRoomVoices(@PathVariable Long roomId, Pageable pageable,@AuthenticationPrincipal UserDetails userDetail){
        return new ResponseEntity<>(roomService.getRoomVoices(roomId,pageable,userDetail), HttpStatus.OK);
    }
}
