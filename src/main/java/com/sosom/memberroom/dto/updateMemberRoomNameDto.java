package com.sosom.memberroom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class updateMemberRoomNameDto {
    @NotEmpty
    private String roomName;
}
