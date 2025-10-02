package com.example.db_setup.service;

import com.example.db_setup.model.Admin;
import com.example.db_setup.model.Player;
import com.example.db_setup.model.RefreshToken;
import com.example.db_setup.model.repository.RefreshTokenRepository;
import com.example.db_setup.security.AuthenticationPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationPropertiesConfig authProperties;

    public ResponseCookie generateRefreshToken(Player player) {
        return generateTokenForUser(null, player);
    }

    public ResponseCookie generateRefreshToken(Admin admin) {
        return generateTokenForUser(admin, null);
    }

    private ResponseCookie generateTokenForUser(Admin admin, Player player) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setAdmin(admin);
        refreshToken.setPlayer(player);
        refreshToken.setExpiryDate(Instant.now().plusMillis(authProperties.getJwtRefreshCookieExpirationMs()));


        List<RefreshToken> oldUserRefreshTokens = admin != null ?
                refreshTokenRepository.findByAdmin(admin) :
                refreshTokenRepository.findByPlayer(player);

        for (RefreshToken oldRefreshToken : oldUserRefreshTokens)
            this.rotate(oldRefreshToken);

        refreshTokenRepository.save(refreshToken);
        return ResponseCookie.from(authProperties.getJwtRefreshCookieName(), refreshToken.getToken()).path("/").maxAge(refreshToken.getExpiryDate().getEpochSecond()).build();

    }

    public ResponseCookie generateCleanRefreshToken() {
        return ResponseCookie.from(authProperties.getJwtRefreshCookieName(), "").path("/").maxAge(0).build();
    }

    public RefreshToken verifyToken(String refreshToken) {
        Optional<RefreshToken> savedToken = refreshTokenRepository.findByToken(refreshToken);
        if (savedToken.isPresent()) {
            if (savedToken.get().getExpiryDate().isAfter(Instant.now())) {
                return savedToken.get();
            } else {
                RefreshToken token = savedToken.get();
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            }
        }

        return null;
    }

    public void invalidAllUserRefreshTokens(Player player) {
        refreshTokenRepository.findByPlayer(player).forEach(this::rotate);
    }

    public void invalidAllAdminRefreshTokens(Admin admin) {
        refreshTokenRepository.findByAdmin(admin).forEach(this::rotate);
    }

    private RefreshToken rotate(RefreshToken oldRefreshToken) {
        oldRefreshToken.setRevoked(true);
        return refreshTokenRepository.save(oldRefreshToken);
    }
}
