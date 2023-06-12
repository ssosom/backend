package com.sosom.member.service;

import com.sosom.exception.CustomException;
import com.sosom.exception.ErrorCode;
import com.sosom.member.domain.Member;
import com.sosom.member.dto.ChangeNicknameRequest;
import com.sosom.member.dto.LoginRequest;
import com.sosom.member.dto.GetMemberInfoDto;
import com.sosom.member.dto.SaveMemberRequest;
import com.sosom.member.repository.MemberRepository;
import com.sosom.response.Result;
import com.sosom.response.dto.IdDto;
import com.sosom.security.jwt.JwtTokenUtil;
import com.sosom.security.jwt.TokenInfo;
import com.sosom.security.jwt.domain.RefreshToken;
import com.sosom.security.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.token.secret}")
    private String secretKey;

    @Transactional
    public IdDto saveMember(SaveMemberRequest saveMemberRequest) {
        String email = saveMemberRequest.getEmail();
        String nickname = saveMemberRequest.getNickname();

        validateExistEmail(email);
        validateExistNickname(nickname);

        String password = passwordEncoder.encode(saveMemberRequest.getPassword());

        Member member =  Member.createNormalMember(email,password,nickname);

        memberRepository.save(member);

        return new IdDto(member.getId());
    }

    public Result<GetMemberInfoDto> getMemberInfo(String email) {
        Member member = validateMember(email);

        return new Result<>(new GetMemberInfoDto(member.getNickname()));
    }

    @Transactional
    public TokenInfo login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Member findMember = memberRepository.findOptionalByEmail(email)
                .orElseThrow(()->new CustomException(ErrorCode.EMAIL_PASSWORD_NOT_FOUND));

        if(!passwordEncoder.matches(password, findMember.getPassword())){
            throw new CustomException(ErrorCode.EMAIL_PASSWORD_NOT_FOUND);
        }

        TokenInfo tokenInfo = JwtTokenUtil.createJwt(email,findMember.getRole().name(),secretKey);

        Optional<RefreshToken> findRefreshToken = refreshTokenRepository.findOptionalByEmail(email);
        if(findRefreshToken.isPresent()){
            findRefreshToken.get().updateRefreshToken(tokenInfo.getRefreshToken());
        }else{
            RefreshToken refreshToken = new RefreshToken(email,tokenInfo.getRefreshToken());
            refreshTokenRepository.save(refreshToken);
        }

        return tokenInfo;
    }

    @Transactional
    public TokenInfo refresh(TokenInfo tokenInfo) {
        String accessToken = tokenInfo.getAccessToken();
        String refreshToken = tokenInfo.getRefreshToken();

        String email = JwtTokenUtil.getEmail(accessToken,secretKey);
        Member findMember = memberRepository.findByEmail(email);
        Optional<RefreshToken> findRefreshToken = refreshTokenRepository.findOptionalByEmailAndRefreshToken(email,refreshToken);

        if(findRefreshToken.isEmpty()){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if(JwtTokenUtil.isExpired(refreshToken,secretKey)){
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        TokenInfo response = JwtTokenUtil.createJwt(email,findMember.getRole().name(),secretKey);
        findRefreshToken.get().updateRefreshToken(response.getRefreshToken());

        return response;
    }

    @Transactional
    public void changeNickname(ChangeNicknameRequest changeNicknameRequest, String email) {
        Member member = validateMember(email);

        String nickname = changeNicknameRequest.getNickname();
        validateExistNickname(nickname);

        member.changeNickname(nickname);
    }

    public Boolean existNickname(String nickname) {
        Optional<Member> findMember = memberRepository.findOptionalByNickname(nickname);

        return findMember.isPresent();
    }

    public Boolean existEmail(String email) {
        Optional<Member> findMember = memberRepository.findOptionalByEmail(email);

        return findMember.isPresent();
    }

    private void validateExistEmail(String email) {
        if(memberRepository.findOptionalByEmail(email).isPresent()){
            throw new CustomException(ErrorCode.EXIST_EMAIL);
        }
    }

    private void validateExistNickname(String nickname) {
        if(memberRepository.findOptionalByNickname(nickname).isPresent()){
            throw new CustomException(ErrorCode.EXIST_NICKNAME);
        }
    }

    private Member validateMember(String email){
        Optional<Member> findMember = memberRepository.findOptionalByEmail(email);

        if(findMember.isEmpty()){
            log.error("확인되지 않은 사용자입니다");
            throw new CustomException(ErrorCode.FAIL_AUTHORIZATION);
        }
        return findMember.get();
    }

}
