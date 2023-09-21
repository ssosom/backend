package com.sosom.member.controller;

import com.sosom.exception.ErrorResponse;
import com.sosom.member.dto.ChangeNicknameRequest;
import com.sosom.member.dto.LoginRequest;
import com.sosom.member.dto.GetMemberInfoDto;
import com.sosom.member.dto.SaveMemberRequest;
import com.sosom.member.service.MemberService;
import com.sosom.response.Result;
import com.sosom.response.dto.IdDto;
import com.sosom.security.jwt.TokenInfo;
import com.sosom.security.userdetails.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Tag(name = "member",description = "사용자에 관한 API")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/api/members")
    @Operation(summary = "회원가입",description = "email,password,nickname를 받아 회원가입 합니다")
    @ApiResponses(value = @ApiResponse(responseCode = "201", description = "회원가입된 Member의 아이디를 반환합니다"))
    public ResponseEntity<IdDto> saveMember(@RequestBody @Valid SaveMemberRequest saveMemberRequest){
        return new ResponseEntity<>(memberService.saveMember(saveMemberRequest),HttpStatus.CREATED);
    }

    @GetMapping("/api/members")
    @Operation(summary = "회원정보",description = "회원정보를 반환합니다")
    public ResponseEntity<Result<GetMemberInfoDto>> getMemberInfo(@AuthenticationPrincipal UserDetails userDetail){
        return new ResponseEntity<>(memberService.getMemberInfo(userDetail.getUsername()),HttpStatus.OK);
    }

    @PostMapping("/api/login")
    @Operation(summary = "로그인",description = "email,password를 받아 로그인 합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "accessToken과 refreshToken를 반환합니다"),
            @ApiResponse(responseCode = "400",description = " email이나 password과 틀릴시 Error를 반환합니다",content = @Content( schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<TokenInfo> login(@RequestBody @Valid LoginRequest loginRequest){
        return new ResponseEntity<>(memberService.login(loginRequest),HttpStatus.OK);
    }

    @PutMapping("/api/refresh")
    @Operation(summary = "refresh 토큰 및 access 토큰 재발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",description = "재발급 된 accessToken과 refresh토큰을 반환합니다"),
            @ApiResponse(responseCode = "401",description = "잘못된 token일 시 401코드를 반환합니다",content = @Content( schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401_2",description = "만료된 refreshToken일시 401코드를 반환합니다",content = @Content( schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TokenInfo> refresh(@RequestBody TokenInfo tokenInfo){
        return new ResponseEntity<>(memberService.refresh(tokenInfo),HttpStatus.CREATED);
    }

    @GetMapping("/api/members/nicknames")
    @Operation(summary = "nickname 중복 체크")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "중복된 닉네임이 있을시 true, 없을 시 false"))
    public ResponseEntity<Boolean> existNickname(@RequestParam String nickname){
        return new ResponseEntity<>(memberService.existNickname(nickname),HttpStatus.OK);
    }

    @GetMapping("/api/members/emails")
    @Operation(summary = "email 중복 체크")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "중복된 이메일이 있을시 true, 없을 시 false"))
    public ResponseEntity<Boolean> existEmail(@RequestParam String email){
        return new ResponseEntity<>(memberService.existEmail(email),HttpStatus.OK);
    }

    @PutMapping("/api/members/nicknames")
    @Operation(summary = "nickname 변경",description = "nickname을 변경합니다")
    @SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "nickname 변경 성공시 201 상태 코드를 반환합니다"),
            @ApiResponse(responseCode = "400",description = " 중복된 nickname이 있을 시 Error를 반환합니다",content = @Content( schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<HttpStatus> changeNickname(
            @RequestBody @Valid ChangeNicknameRequest changeNicknameRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails){
        memberService.changeNickname(changeNicknameRequest,userDetails.getUsername());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
