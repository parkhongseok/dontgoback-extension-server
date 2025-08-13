package com.dontgoback.msa.extension.config.interserverauth;

import com.dontgoback.msa.extension.config.interserverauth.jwt.InterServerJwtVerifier;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
* 이 필터는 Core 서버가 네임 서버로부터의 요청을 수신할 때,
* 해당 요청의 JWT를 추출하고 검증하여
* SecurityContext에 인증 객체를 등록하는 역할
*/

@Slf4j
//@Component -> 컴포넌트 제거 후 config에서 직접 주입 왜냐면 자꾸 필터가 전역으로 적용됨
@RequiredArgsConstructor
public class InterServerAuthenticationFilter extends OncePerRequestFilter {

    private final InterServerJwtVerifier jwtVerifier;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String uri = request.getRequestURI();
//        // base-path 바꿨다면 "/actuator/" 대신 그 경로로
//        return uri.startsWith("/actuator/");
//    }

    @Override
    protected void doFilterInternal(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 헤더에서 JWT 받아오기
            String token = extractToken(request);
            // 토큰 검증 및 내용 반환
            Claims claims = jwtVerifier.parseAndValidate(token);
            // 인증 객체 생성 (권한은 없음, clientId만 포함)
            Authentication authentication = jwtVerifier.getAuthentication(claims);
            // SecurityContext 에 Authentication 객체를 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("서버 간 인증 성공");
        } catch (Exception e) {
            log.error("서버 간 인증 필터 오류", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    private String extractToken(HttpServletRequest request){
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)){
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
