package com.sosom.security.jwt;

import com.sosom.exception.CustomException;
import com.sosom.exception.ErrorCode;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;


@Slf4j
public class JwtTokenUtil {

    private static final Long accessExpiredMs = 1000 * 60 * 10L;

    private static final Long refreshExpiredMs = 1000 * 60 * 60 * 24 * 14L;

    public static TokenInfo createJwt(String email, String role, String secretKey){
        Claims claims = Jwts.claims();
        claims.put("email",email);
        claims.put("role",role);

        String accessToken =Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+accessExpiredMs))
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+refreshExpiredMs))
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .compact();

        return new TokenInfo(accessToken,refreshToken);
    }

    public static boolean isExpired(String token,String secretKey){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().getExpiration().before(new Date());
    }

    public static String getEmail(String token,String secretKey){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().get("email",String.class);
    }

    public static String getRole(String token,String secretKey){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().get("role",String.class);
    }

    public static boolean validateJwtToken(String token,String secretKey){
        try{
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (SignatureException | MalformedJwtException | IllegalArgumentException e){
            log.error("INVALID_TOKEN");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e){
            throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        }
    }
}
