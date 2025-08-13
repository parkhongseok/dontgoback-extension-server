package com.dontgoback.msa.extension.config.interserverauth;

import com.dontgoback.msa.extension.config.interserverauth.jwt.InterServerJwtVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class InterServerSecurityConfig {
    private final InterServerJwtVerifier jwtVerifier;
    /**
     * Actuator health check 엔드포인트에 대한 익명 접근을 허용
     * 가장 우선순위가 높은(@Order(0)) 필터 체인으로 설정하여 다른 보안 규칙보다 먼저 처리되도록 함
     */
    @Bean
    @Order(0)
    public SecurityFilterChain actuatorHealthFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(EndpointRequest.toAnyEndpoint()) // /actuator/** 전체를 이 체인으로
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll() // health만 개방
                        .anyRequest().denyAll() // 나머지 actuator는 차단(필요시 조정)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain interServerSecurityFilter(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/msa/ext/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new InterServerAuthenticationFilter(jwtVerifier), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
