package com.sosom.memberroom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetRoomsDto {

    private Long roomId;

    private Long memberRoomId;

    private String roomName;

    private LocalDateTime lastActive;
}
