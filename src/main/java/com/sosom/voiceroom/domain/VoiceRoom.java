package com.sosom.voiceroom.domain;

import com.sosom.room.domain.Room;
import com.sosom.voice.domain.Voice;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoiceRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VOICE_ROOM_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VOICE_ID")
    private Voice voice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_ID")
    private Room room;

    private VoiceRoom(Voice voice, Room room){
        this.voice = voice;
        this.room = room;
    }

    public static VoiceRoom createVoiceRoom(Voice voice,Room room){
        return new VoiceRoom(voice,room);
    }

}
