package com.sosom.room.dto;

import com.sosom.member.domain.Member;
import com.sosom.voiceroom.domain.VoiceRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetRoomVoicesDto {
    private String url;
    private String message;
    private boolean myVoice = false;

    public GetRoomVoicesDto(VoiceRoom voiceRoom, Member member){
        this.url = voiceRoom.getVoice().getUrl();
        this.message = voiceRoom.getVoice().getMessage();

        if(voiceRoom.getVoice().getMember().equals(member)){
            this.myVoice = true;
        }
    }
}
