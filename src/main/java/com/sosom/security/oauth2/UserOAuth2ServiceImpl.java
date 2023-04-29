package com.sosom.security.oauth2;

import com.sosom.member.domain.Member;
import com.sosom.member.domain.SignUpType;
import com.sosom.member.repository.MemberRepository;
import com.sosom.security.userdetails.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
public class UserOAuth2ServiceImpl extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String,Object> account = new HashMap<>();
        Member member;

        switch (provider){
            case "kakao":
                account = oAuth2User.getAttribute("kakao_account");
                break;
            case "naver":
                account = oAuth2User.getAttribute("response");
                break;
            default:
                break;
        }
        String email = (String) account.get("email");


        Optional<Member> findMember = memberRepository.findOptionalByEmail(email);

        if(findMember.isEmpty()){
            String password = passwordEncoder.encode(UUID.randomUUID().toString());
            String nickname = UUID.randomUUID().toString();
            member = Member.createSocialMember(email,password,nickname,SignUpType.valueOf(provider.toUpperCase()));
            memberRepository.save(member);
        }else{
            member = findMember.get();
            if(member.getSignUpType() == SignUpType.NORMAL){
                throw new OAuth2AuthenticationException("일반 회원으로 가입된 회원입니다.");
            }
            if(member.getSignUpType() != SignUpType.valueOf(provider.toUpperCase())){
                throw new OAuth2AuthenticationException(provider+"회원으로 가입된 회원입니다.");
            }
        }

        return new UserDetailsImpl(member.getEmail(),member.getPassword(),member.getRole(),account);
    }
}
