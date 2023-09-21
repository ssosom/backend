package com.sosom.memberroom.controller;

import com.sosom.memberroom.dto.updateMemberRoomNameDto;
import com.sosom.memberroom.service.MemberRoomService;
import com.sosom.response.Result;
import com.sosom.memberroom.dto.GetRoomsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "memberRoom",description = "개개인의 사용자들의 대화방에 관한 api")
public class MemberRoomController {

    private final MemberRoomService memberRoomService;

    @Operation(summary = "본인이 속한 대화방 목록",description = "본인이 속한 대화방의 목록을 모두 보여줍니다")
    @GetMapping("/members/rooms")
    public ResponseEntity<Result<List<GetRoomsDto>>> getRooms(@AuthenticationPrincipal UserDetails userDetail){
        return new ResponseEntity<>(memberRoomService.getRooms(userDetail), HttpStatus.OK);
    }

    @Operation(summary = "대화방의 이름을 변경",description = "대화방의 아이디와 대화방의 이름을 받아 대화방의 이름을 변경합니다")
    @PatchMapping("/members/rooms/{memberRoomId}")
    public ResponseEntity<Void> updateMemberRoomName(@PathVariable Long memberRoomId,
                                                     @Valid @RequestBody updateMemberRoomNameDto updateMemberRoomNameDto,
                                                     @AuthenticationPrincipal UserDetails userDetail){
        return memberRoomService.updateMemberRoomName(memberRoomId,updateMemberRoomNameDto,userDetail.getUsername());
    }

    @Operation(summary = "대화방에서 나가기",description = "대화방의 아이디를 받아 대화방에서 나갑니다")
    @DeleteMapping("/members/rooms/{memberRoomId}")
    public ResponseEntity<Void> leaveMemberRoom(@PathVariable Long memberRoomId,@AuthenticationPrincipal UserDetails userDetail){
        return memberRoomService.leaveMemberRoom(memberRoomId,userDetail.getUsername());
    }

}
