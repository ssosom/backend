package com.sosom.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceRequestDto {
    @NotEmpty
    private String voice;
    @NotEmpty
    private String message;
    @NotEmpty
    private String type;
}
