package com.sosom.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "email를 입력해주세요")
    private String email;

    @NotBlank(message = "password를 입력해주세요")
    private String password;
}
