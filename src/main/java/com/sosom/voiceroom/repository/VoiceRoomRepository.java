package com.sosom.voiceroom.repository;

import com.sosom.room.domain.Room;
import com.sosom.voiceroom.domain.VoiceRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoiceRoomRepository extends JpaRepository<VoiceRoom,Long> {
    @Query("select vr from VoiceRoom as vr join fetch vr.voice as v where vr.room = :room order by v.id desc ")
    Slice<VoiceRoom> findByRoom(@Param("room") Room room, Pageable pageable);
}
