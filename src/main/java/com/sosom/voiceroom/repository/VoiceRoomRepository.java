package com.sosom.voiceroom.repository;

import com.sosom.voiceroom.domain.VoiceRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoiceRoomRepository extends JpaRepository<VoiceRoom,Long> {
}
