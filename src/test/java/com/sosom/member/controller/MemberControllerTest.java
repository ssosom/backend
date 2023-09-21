package com.sosom.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosom.anotation.WithCustomUserDetails;
import com.sosom.exception.CustomException;
import com.sosom.exception.ErrorCode;
import com.sosom.member.dto.GetMemberInfoDto;
import com.sosom.member.dto.LoginRequest;
import com.sosom.member.dto.SaveMemberRequest;
import com.sosom.member.dto.ChangeNicknameRequest;
import com.sosom.member.service.MemberService;
import com.sosom.response.Result;
import com.sosom.response.dto.IdDto;
import com.sosom.security.jwt.TokenInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = MemberController.class)
@WithCustomUserDetails(email = "test@email.com", role = "ROLE_MEMBER")
class MemberControllerTest {

    @MockBean
    private MemberService memberService;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    @Nested
    @DisplayName("회원가입")
    class saveMember{

        @Nested
        @DisplayName("성공")
        class success{

            @Test
            @DisplayName("정상적인 회원가입")
            void success_response() throws Exception {
                //given
                SaveMemberRequest saveMemberRequest = new SaveMemberRequest("email","password","nickname");
                IdDto result = new IdDto(1L);
                when(memberService.saveMember(any())).thenReturn(result);

                //when,then
                mockMvc.perform(
                                post("/api/members")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(saveMemberRequest))
                                        .with(csrf()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(1L))
                        .andDo(MockMvcResultHandlers.print());

            }
        }

        @Nested
        @DisplayName("실패")
        class fail{

            @Test
            @DisplayName("email이 빈칸인 경우")
            void fail_emailEmpty() throws Exception {
                //given
                SaveMemberRequest saveMemberRequest = new SaveMemberRequest("","password","nickname");


                //when,then
                mockMvc.perform(
                                post("/api/members")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(saveMemberRequest))
                                        .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorName").value(HttpStatus.BAD_REQUEST.name()))
                        .andExpect(jsonPath("$.message").value("email를 입력해주세요"))
                        .andDo(MockMvcResultHandlers.print());
            }

            @Test
            @DisplayName("password가 빈칸인 경우")
            void fail_passwordEmpty() throws Exception {
                //given
                SaveMemberRequest saveMemberRequest = new SaveMemberRequest("email","","nickname");


                //when,then
                mockMvc.perform(
                                post("/api/members")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(saveMemberRequest))
                                        .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorName").value(HttpStatus.BAD_REQUEST.name()))
                        .andExpect(jsonPath("$.message").value("password를 입력해주세요"))
                        .andDo(MockMvcResultHandlers.print());
            }

            @Test
            @DisplayName("nickname이 빈칸인 경우")
            void fail_nicknameEmpty() throws Exception {
                //given
                SaveMemberRequest saveMemberRequest = new SaveMemberRequest("email","password","");


                //when,then
                mockMvc.perform(
                                post("/api/members")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(saveMemberRequest))
                                        .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorName").value(HttpStatus.BAD_REQUEST.name()))
                        .andExpect(jsonPath("$.message").value("nickname을 입력해주세요"))
                        .andDo(MockMvcResultHandlers.print());
            }
        }
    }
    @Nested
    @DisplayName("회원정보")
    class getMemberInfo{

        @Nested
        @DisplayName("성공")
        class success{
            @Test
            @DisplayName("정상적인 회원정보 반환")
            void success_get_member_info() throws Exception {
                //given
                Result<GetMemberInfoDto> result = new Result<>(new GetMemberInfoDto("nickname"));
                given(memberService.getMemberInfo(any())).willReturn(result);

                //when,then
                mockMvc.perform(
                        get("/api/members"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.nickname").value("nickname"))
                        .andDo(MockMvcResultHandlers.print());
            }

        }

        @Nested
        @DisplayName("실패")
        class fail{

            @Test
            @DisplayName("회원이 아닌 경우")
            void  fail_not_member() throws Exception {
                //given
                when(memberService.getMemberInfo(any())).thenThrow(new CustomException(ErrorCode.FAIL_AUTHORIZATION));

                //when,then
                mockMvc.perform(
                                get("/api/members"))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.errorName").value(ErrorCode.FAIL_AUTHORIZATION.name()))
                        .andExpect(jsonPath("$.message").value(ErrorCode.FAIL_AUTHORIZATION.getMessage()))
                        .andDo(MockMvcResultHandlers.print());
            }

        }

    }

    @Nested
    @DisplayName("로그인")
    class login {

        @Nested
        @DisplayName("성공")
        class success{
            @Test
            @DisplayName("정상적인 로그인")
            void success_login() throws Exception{
                //given
                LoginRequest loginRequest = new LoginRequest("email","password");
                TokenInfo tokenInfo  = new TokenInfo("accessToken","refreshToken");
                when(memberService.login(any())).thenReturn(tokenInfo);

                //when,then
                mockMvc.perform(
                                post("/api/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginRequest))
                                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").value("accessToken"))
                        .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                        .andDo(MockMvcResultHandlers.print());
            }
        }

        @Nested
        @DisplayName("실패")
        class fail{

            @Test
            @DisplayName("email이 빈칸인 경우")
            void fail_empty_email() throws Exception{
                //given
                LoginRequest loginRequest = new LoginRequest("","password");

                //when,then
                mockMvc.perform(
                                post("/api/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginRequest))
                                        .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorName").value(HttpStatus.BAD_REQUEST.name()))
                        .andExpect(jsonPath("$.message").value("email를 입력해주세요"))
                        .andDo(MockMvcResultHandlers.print());
            }

            @Test
            @DisplayName("비밀번호가 빈칸인 경우")
            void fail_empty_password() throws Exception{
                //given
                LoginRequest loginRequest = new LoginRequest("email","");

                //when,then
                mockMvc.perform(
                                post("/api/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginRequest))
                                        .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorName").value(HttpStatus.BAD_REQUEST.name()))
                        .andExpect(jsonPath("$.message").value("password를 입력해주세요"))
                        .andDo(MockMvcResultHandlers.print());
            }

            @Test
            @DisplayName("email 또는 password가 틀린 경우")
            void fail_email_password_not_found() throws Exception{
                //given
                LoginRequest loginRequest = new LoginRequest("email","password");
                when(memberService.login(any())).thenThrow(new CustomException(ErrorCode.EMAIL_PASSWORD_NOT_FOUND));

                //when,then
                mockMvc.perform(
                                post("/api/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginRequest))
                                        .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorName").value(ErrorCode.EMAIL_PASSWORD_NOT_FOUND.name()))
                        .andExpect(jsonPath("$.message").value(ErrorCode.EMAIL_PASSWORD_NOT_FOUND.getMessage()))
                        .andDo(MockMvcResultHandlers.print());
            }

        }

    }


    @Nested
    @DisplayName("닉네임 중복 확인")
    class existNickname{

        @Nested
        @DisplayName("성공")
        class success{
            @Test
            @DisplayName("중복된 닉네임이 없는 경우")
            void success_notExist() throws Exception {
                //given
                when(memberService.existNickname(any())).thenReturn(false);

                //when,then
                mockMvc.perform(
                                get("/api/members/nicknames")
                                        .param("nickname", "nickname"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("false"))
                        .andDo(MockMvcResultHandlers.print());
            }

            @Test
            @DisplayName("중복된 닉네임이 있는 경우")
            void success_exist() throws Exception {
                //given
                when(memberService.existNickname(any())).thenReturn(true);

                //when,then
                mockMvc.perform(
                                get("/api/members/nicknames")
                                        .param("nickname", "nickname"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("true"))
                        .andDo(MockMvcResultHandlers.print());
            }
        }
    }

    @Nested
    @DisplayName("이메일 중복 확인")
    class existEmail{

        @Nested
        @DisplayName("성공")
        class success{

            @Test
            @DisplayName("중복된 이메일이 없는 경우")
            void success_notExist() throws Exception{
                //given
                when(memberService.existEmail(any())).thenReturn(false);

                //when,then
                mockMvc.perform(
                                get("/api/members/emails")
                                        .param("email", "email"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("false"))
                        .andDo(MockMvcResultHandlers.print());

            }

            @Test
            @DisplayName("중복된 이메일이 있는 경우")
            void success_exist() throws Exception {
                //given
                when(memberService.existEmail(any())).thenReturn(true);

                //when,then
                mockMvc.perform(
                                get("/api/members/emails")
                                        .param("email", "email"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("true"))
                        .andDo(MockMvcResultHandlers.print());
            }
        }
    }

    @Nested
    @DisplayName("닉네임 변경")
    class nicknameChange{

        @Nested
        @DisplayName("성공")
        class success{
            @Test
            @DisplayName("올바른 닉네임 변경")
            void success_change() throws Exception{
                //given
                ChangeNicknameRequest changeNicknameRequest = new ChangeNicknameRequest("change");
                doNothing().when(memberService).changeNickname(any(),any());

                //when,then
                mockMvc.perform(
                        put("/api/members/nicknames")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(changeNicknameRequest))
                                .with(csrf()))
                        .andExpect(status().isCreated())
                        .andDo(MockMvcResultHandlers.print());
            }

        }

        @Nested
        @DisplayName("실패")
        class fail{
            @Test
            @DisplayName("중복된 닉네임이 있는 경우")
            void  fail_nickname_exist() throws Exception{
                //given
                ChangeNicknameRequest changeNicknameRequest = new ChangeNicknameRequest("change");
                doThrow(new CustomException(ErrorCode.EXIST_NICKNAME)).when(memberService).changeNickname(any(),any());

                //when,then
                mockMvc.perform(
                        put("/api/members/nicknames")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(changeNicknameRequest))
                                .with(csrf()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errorName").value(ErrorCode.EXIST_NICKNAME.name()))
                        .andExpect(jsonPath("$.message").value(ErrorCode.EXIST_NICKNAME.getMessage()))
                        .andDo(MockMvcResultHandlers.print());
            }

        }

    }
}