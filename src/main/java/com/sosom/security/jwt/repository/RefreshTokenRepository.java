package com.sosom.security.jwt.repository;

import com.sosom.security.jwt.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findOptionalByEmail(String email);
    Optional<RefreshToken> findOptionalByEmailAndRefreshToken(String email,String refreshToken);
}
