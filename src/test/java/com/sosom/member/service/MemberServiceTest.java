package com.sosom.member.service;

import com.sosom.exception.CustomException;
import com.sosom.exception.ErrorCode;
import com.sosom.member.domain.Member;
import com.sosom.member.dto.ChangeNicknameRequest;
import com.sosom.member.dto.LoginRequest;
import com.sosom.member.dto.SaveMemberRequest;
import com.sosom.member.repository.MemberRepository;
import com.sosom.response.dto.IdDto;
import com.sosom.security.jwt.JwtTokenUtil;
import com.sosom.security.jwt.TokenInfo;
import com.sosom.security.jwt.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    private Member member;

    @BeforeEach
    public void setUp(){
        member =  Member.createNormalMember("email",passwordEncoder.encode("password"),"nickname");
    }


    @Test
    @DisplayName("회원가입")
    void saveMember(){
        //given
        SaveMemberRequest saveMemberRequest  = new SaveMemberRequest("email","password","nickname");
        given(memberRepository.save(any())).willReturn(member);

        //when
        IdDto idDto = memberService.saveMember(saveMemberRequest);

        //then
        assertThat(idDto.getId()).isEqualTo(member.getId());
    }

    @Nested
    @DisplayName("로그인")
    class login {

        @Nested
        @DisplayName("성공")
        class success {
            @Test
            @DisplayName("정상적인 로그인")
            void success_login() {
                //given
                LoginRequest loginRequest = new LoginRequest("email", "password");
                String secretKey = "testSecretKey";
                ReflectionTestUtils.setField(memberService, "secretKey", secretKey);
                given(memberRepository.findOptionalByEmail(any())).willReturn(Optional.of(member));

                //when
                TokenInfo tokenInfo = memberService.login(loginRequest);

                //then
                assertThat(JwtTokenUtil.getEmail(tokenInfo.getAccessToken(), secretKey)).isEqualTo("email");
            }

        }


        @Nested
        @DisplayName("실패")
        class fail {

            @Test
            @DisplayName("email이 틀린 경우")
            void fail_invalid_email() {
                //given
                LoginRequest loginRequest = new LoginRequest("invalid_email", "password");
                given(memberRepository.findOptionalByEmail(any())).willReturn(Optional.empty());

                //when
                CustomException customException = assertThrows(CustomException.class, () -> memberService.login(loginRequest));

                //then
                assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_PASSWORD_NOT_FOUND);

            }

            @Test
            @DisplayName("password가 틀린 경우")
            void fail_invalid_password() {
                //given
                LoginRequest loginRequest = new LoginRequest("email", "invalid_password");
                given(memberRepository.findOptionalByEmail(any())).willReturn(Optional.of(member));

                //when
                CustomException customException = assertThrows(CustomException.class, () -> memberService.login(loginRequest));

                //then
                assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_PASSWORD_NOT_FOUND);
            }

        }
    }

        @Nested
        @DisplayName("닉네임 변경")
        class changeNickname{

            @Nested
            @DisplayName("성공")
            class success{
                @Test
                @DisplayName("정상적인 닉네임 변경")
                void success_change_nickname(){
                    //given
                    ChangeNicknameRequest changeNicknameRequest = new ChangeNicknameRequest("changeNickname");
                    String email = "email";
                    given(memberRepository.findOptionalByNickname(any())).willReturn(Optional.empty());
                    given(memberRepository.findByEmail(email)).willReturn(member);

                    //when
                    memberService.changeNickname(changeNicknameRequest,email);

                    //then
                    assertThat(member.getNickname()).isEqualTo(changeNicknameRequest.getNickname());
                }

            }

            @Nested
            @DisplayName("실패")
            class fail{
                @Test
                @DisplayName("바꾸려는 닉네임을 가진 멤버가 존재하는 경우")
                void fail_exist_nickname(){
                    //given
                    ChangeNicknameRequest changeNicknameRequest = new ChangeNicknameRequest("nickname");
                    String email = "newEmail";
                    given(memberRepository.findOptionalByNickname(any())).willReturn(Optional.of(member));

                    //when
                    CustomException customException = assertThrows(CustomException.class, () -> memberService.changeNickname(changeNicknameRequest, email));

                    //then
                    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.EXIST_NICKNAME);
                }

            }

        }

        @Nested
        @DisplayName("닉네임 중복 확인")
        class existNickname{

            @Test
            @DisplayName("닉네임 중복이 있는 경우")
            void exist_nickname(){
                //given
                String nickname = "nickname";
                given(memberRepository.findOptionalByNickname(nickname)).willReturn(Optional.of(member));

                //when
                Boolean result = memberService.existNickname(nickname);

                //then
                assertThat(result).isTrue();
            }

            @Test
            @DisplayName("닉네임 중복이 없는 경우")
            void not_exist_nickname(){
                //given
                String nickname = "newNickname";
                given(memberRepository.findOptionalByNickname(nickname)).willReturn(Optional.empty());

                //when
                Boolean result = memberService.existNickname(nickname);

                //then
                assertThat(result).isFalse();
            }

        }

        @Nested
        @DisplayName("이메일 중복 확인")
        class existEmail{

            @Test
            @DisplayName("이메일 중복이 있는 경우")
            void exist_nickname(){
                //given
                String email = "email";
                given(memberRepository.findOptionalByEmail(email)).willReturn(Optional.of(member));

                //when
                Boolean result = memberService.existEmail(email);

                //then
                assertThat(result).isTrue();
            }

            @Test
            @DisplayName("이메일 중복이 없는 경우")
            void not_exist_nickname(){
                //given
                String email = "newEmail";
                given(memberRepository.findOptionalByEmail(email)).willReturn(Optional.empty());

                //when
                Boolean result = memberService.existEmail(email);

                //then
                assertThat(result).isFalse();
            }

        }
}