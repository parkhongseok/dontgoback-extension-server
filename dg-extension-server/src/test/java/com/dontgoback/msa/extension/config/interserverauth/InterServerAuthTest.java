package com.dontgoback.msa.extension.config.interserverauth;

import com.dontgoback.msa.extension.config.interserverauth.jwt.InterServerJwtVerifier;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // ✅ static 없이도 가능!
public class InterServerAuthTest {
    @Autowired
    private InterServerTestProperties testProps;

    @Autowired
    private InterServerKeyTokenTestProvider provider;

    @Autowired
    private InterServerJwtVerifier verifier;

    private final String HEADER_AUTHORIZATION = "Authorization";
    private final String TOKEN_PREFIX = "Bearer ";

    private String token;


    @BeforeAll
    void setup() throws Exception {
        token = provider.getToken();
        System.out.println("🔐 발급받은 JWT (setup): " + token);
    }

    @Test
    void 공개키_로딩_성공_테스트() throws Exception{
        PublicKey publicKey = provider.getPublicKey();
        assertNotNull(publicKey);
        System.out.println("✅ 공개키 확인: " + publicKey.getAlgorithm());
    }

    @Test
    void 발급_토큰_출력_테스트() {
        assertNotNull(token);
        System.out.println("✅ 저장된 토큰 사용: " + token);
    }

    @Test
    void 토큰_유효성_검사_테스트() {
        assertNotNull(verifier.parseAndValidate(token));
        System.out.println("✅ 토큰 유효성 검증 완료");
    }
}
