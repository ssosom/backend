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
import com.sosom.security.jwt.domain.RefreshToken;
import com.sosom.security.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
        String password = passwordEncoder.encode(saveMemberRequest.getPassword());

        Member member =  Member.createNormalMember(email,password,nickname);

        memberRepository.save(member);

        return new IdDto(member.getId());
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
        String nickname = changeNicknameRequest.getNickname();
        Optional<Member> findMember = memberRepository.findByNickname(nickname);

        if(findMember.isPresent()){
            throw new CustomException(ErrorCode.EXIST_NICKNAME);
        }

        Member member = memberRepository.findByEmail(email);

        member.changeNickname(nickname);
    }

    public Boolean existNickname(String nickname) {
        Optional<Member> findMember = memberRepository.findByNickname(nickname);

        return findMember.isPresent();
    }

    public Boolean existEmail(String email) {
        Optional<Member> findMember = memberRepository.findOptionalByEmail(email);

        return findMember.isPresent();
    }
}
