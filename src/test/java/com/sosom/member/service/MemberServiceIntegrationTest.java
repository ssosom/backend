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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class MemberServiceIntegrationTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MemberService memberService;

    @Value("${jwt.token.secret}")
    private String secretKey;


    @BeforeEach
    public void setUp(){
        Member member  = Member.createNormalMember("email",passwordEncoder.encode("password"),"nickname");
        Member member2 = Member.createNormalMember("newEmail", passwordEncoder.encode("newPassword"), "newNickname");
        memberRepository.save(member);
        memberRepository.save(member2);
    }

    @Test
    @DisplayName("회원 가입")
    void saveMember(){
        //given
        SaveMemberRequest saveMemberRequest  = new SaveMemberRequest("saveEmail","savePassword","saveNickname");

        //when
        IdDto result = memberService.saveMember(saveMemberRequest);
        Member savedMember = memberRepository.findById(result.getId()).get();

        //then
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(savedMember.getEmail()).isEqualTo(saveMemberRequest.getEmail());
        assertThat(passwordEncoder.matches(saveMemberRequest.getPassword(),savedMember.getPassword())).isTrue();
        assertThat(savedMember.getNickname()).isEqualTo(saveMemberRequest.getNickname());
    }

    @Nested
    @DisplayName("로그인")
    class login{

        @Nested
        @DisplayName("성공")
        class success{
            @Test
            @DisplayName("정상적인 로그인")
            void success_login(){
                //given
                LoginRequest loginRequest = new LoginRequest("email","password");

                //when
                TokenInfo result = memberService.login(loginRequest);

                //then
                assertThat(JwtTokenUtil.getEmail(result.getAccessToken(),secretKey)).isEqualTo(loginRequest.getEmail());
            }
        }

        @Nested
        @DisplayName("실패")
        class fail{
            @Test
            @DisplayName("email이 틀린 경우")
            void fail_invalid_email() {
                //given
                LoginRequest loginRequest = new LoginRequest("invalid_email", "password");

                //when
                CustomException customException = assertThrows(CustomException.class,() -> memberService.login(loginRequest));

                //then
                assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_PASSWORD_NOT_FOUND);

            }

            @Test
            @DisplayName("password가 틀린 경우")
            void fail_invalid_password() {
                //given
                LoginRequest loginRequest = new LoginRequest("email", "invalid_password");

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

                //when
                memberService.changeNickname(changeNicknameRequest,email);
                Member findMember = memberRepository.findByEmail(email);

                //then
                assertThat(findMember.getNickname()).isEqualTo(changeNicknameRequest.getNickname());
            }
        }

        @Nested
        @DisplayName("실패")
        class fail{
            @Test
            @DisplayName("바꾸려는 닉네임을 가진 멤버가 존재하는 경우")
            void fail_exist_nickname() {
                //given
                ChangeNicknameRequest changeNicknameRequest = new ChangeNicknameRequest("newNickname");
                String email = "email";

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

            //when
            Boolean result = memberService.existNickname(nickname);

            //then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("닉네임 중복이 없는 경우")
        void not_exist_nickname(){
            //given
            String nickname = "not_exist_nickname";

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

            //when
            Boolean result = memberService.existEmail(email);

            //then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("이메일 중복이 없는 경우")
        void not_exist_nickname(){
            //given
            String email = "not_exist_email";

            //when
            Boolean result = memberService.existEmail(email);

            //then
            assertThat(result).isFalse();
        }
    }


}
