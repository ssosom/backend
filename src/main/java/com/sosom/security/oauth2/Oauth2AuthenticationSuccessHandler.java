package com.sosom.security.oauth2;

import com.sosom.member.domain.Member;
import com.sosom.member.repository.MemberRepository;
import com.sosom.security.jwt.TokenInfo;
import com.sosom.security.jwt.JwtTokenUtil;
import com.sosom.security.jwt.domain.RefreshToken;
import com.sosom.security.jwt.repository.RefreshTokenRepository;
import com.sosom.security.userdetails.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class Oauth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.token.secret}")
    private String secretKey;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String email = userDetails.getUsername();
        String role = String.valueOf(authentication.getAuthorities());

        TokenInfo tokenInfo = JwtTokenUtil.createJwt(email,role,secretKey);
        Optional<RefreshToken> findRefreshToken = refreshTokenRepository.findOptionalByEmail(email);
        if(findRefreshToken.isPresent()){
            findRefreshToken.get().updateRefreshToken(tokenInfo.getRefreshToken());
        }else{
            RefreshToken refreshToken = new RefreshToken(email,tokenInfo.getRefreshToken());
            refreshTokenRepository.save(refreshToken);
        }

        Member member = memberRepository.findByEmail(email);

        String isFirst = "true";
        if (member.getCreatedDate() != member.getLastModifiedDate()) {
            isFirst = "false";
        }

        String url = makeRedirectUrl(isFirst,tokenInfo.getAccessToken(),tokenInfo.getRefreshToken());

        getRedirectStrategy().sendRedirect(request, response, url);
    }

    private String makeRedirectUrl(String isFirst,String accessToken,String refreshToken) {
        return UriComponentsBuilder.fromUriString("/success")
                .queryParam("isFirst", isFirst)
                .queryParam("accessToken",accessToken)
                .queryParam("refreshToken",refreshToken)
                .build().toUriString();
    }



}
