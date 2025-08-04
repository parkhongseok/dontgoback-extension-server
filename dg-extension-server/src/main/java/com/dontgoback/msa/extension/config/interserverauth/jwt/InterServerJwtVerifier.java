package com.dontgoback.msa.extension.config.interserverauth.jwt;


/** 목표
 * 요청에 담긴 JWT를 파싱하고, 서명을 검증하며,
 * 내부적으로 필요한 정보를 추출 (예: clientId, issuer, 만료 시간 등)
 * 서명 검증에는 InterServerPublicKeyManager가 제공하는 공개키를 사용
 */

import com.dontgoback.msa.extension.config.interserverauth.client.InterServerAuthClientProperties;
import com.dontgoback.msa.extension.config.interserverauth.key.InterServerPublicKeyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;

/**
 * 전제 조건
 * 토큰은 RS256 (비대칭키) 기반으로 Name Server에서 발급
 * Core Server는 public key만 갖고 있으므로, 검증만 수행
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class InterServerJwtVerifier {

    private final InterServerJwtProperties jwtProperties;
    private final InterServerAuthClientProperties clientProperties;
    private final InterServerPublicKeyManager publicKeyManager;
    private final UserDetailsService userDetailsService;

    /**
     * 서버 간 인증용 JWT의 유효성을 검증합니다.
     * - 서명 검증
     * - issuer 확인
     * - subject(clientId) 확인
     */
    public Claims parseAndValidate(String token){
        try {
            Claims claims = getClaims(token);

            if (! issuerMatches(claims)) {
                log.warn("JWT issuer 불일치: {}", claims.getIssuer());
                throw new IllegalArgumentException("잘못된 issuer");
            }
            if (!subjectMatches(claims)) {
                log.warn("JWT subject(clientId) 불일치: {}", claims.getSubject());
                throw new IllegalArgumentException("잘못된 clientId");
            }
            return claims;

        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다: {}", e.getMessage());
            throw new IllegalArgumentException("만료된 서버 인증 토큰입니다.");
        } catch (JwtException e) {
            log.warn("JWT 서명 검증 실패 또는 파싱 에러", e);
            throw new IllegalArgumentException("유효하지 않은 서버 인증 토큰입니다.");
        } catch (Exception e) {
            log.error("JWT 파싱 중 알 수 없는 오류", e);
            throw new IllegalStateException("JWT 검증 중 예기치 못한 오류가 발생했습니다.");
        }
    }

    public Authentication getAuthentication(Claims claims){
        String clientId = getClientId(claims);
        UserDetails authUser = userDetailsService.loadUserByUsername(clientId);
        return new UsernamePasswordAuthenticationToken(
                // principal : 인증된 사용자 정보
                // Credentals : 무엇으로 인증했는가 기록하는 부분 (보안을 위해 비워둠)
                authUser, "", authUser.getAuthorities()
        );
    }

    private String getClientId(Claims claims){
        return claims.getSubject();
    }

    private boolean issuerMatches(Claims claims) {
        String issuer = claims.getIssuer();
        String allowedIssuer = jwtProperties.getIssuer();
        return allowedIssuer.equals(issuer);
    }

    private boolean subjectMatches(Claims claims) {
        String clientId = claims.getSubject(); // subject에 clientId를 검증
        String allowedClientId = clientProperties.getId();
        return allowedClientId.equals(clientId);
    }

    private Claims getClaims(String token) {
        RSAPublicKey publicKey = publicKeyManager.getPublicKey();

        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)  // 여기서 자동으로 만료시간도 검증함
                .getBody();
    }
}
