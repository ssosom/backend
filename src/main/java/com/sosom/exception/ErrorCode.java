package com.sosom.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    EXIST_NICKNAME(HttpStatus.BAD_REQUEST,"존재하는 닉네임 입니다"),
    EMAIL_PASSWORD_NOT_FOUND(HttpStatus.BAD_REQUEST,"아이디나 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않은 Token입니다"),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"만료된 AccessToken입니다"),
    FAIL_AUTHORIZATION(HttpStatus.UNAUTHORIZED,"인증되지 않은 사용자입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"만료된 RefreshToken입니다"),
    EXIST_EMAIL(HttpStatus.BAD_REQUEST,"존재하는 이메일입니다"),
    FAIL_VOICE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR,"보이스 업로드에 실패했습니다."),
    NOT_EXIST_ROOM(HttpStatus.BAD_REQUEST,"존재하지 않는 대화방입니다"),
    NOT_EXIST_MEMBER_ROOM(HttpStatus.BAD_REQUEST,"존재하지 않는 대화방입니다"),
    FAIL_AUTHORITY(HttpStatus.FORBIDDEN,"인가되지 않은 사용자입니다");
    private final HttpStatus status;
    private final String message;

}