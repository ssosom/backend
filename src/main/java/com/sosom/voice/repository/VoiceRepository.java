package com.sosom.voice.repository;

import com.sosom.voice.domain.Voice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoiceRepository extends JpaRepository<Voice,Long> {
}
