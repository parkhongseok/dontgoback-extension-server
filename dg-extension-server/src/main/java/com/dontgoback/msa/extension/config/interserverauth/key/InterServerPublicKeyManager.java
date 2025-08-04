package com.dontgoback.msa.extension.config.interserverauth.key;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * InterServerPublicKeyManager는 인증 서버(Auth Server)로부터
 * 비대칭키 방식의 JWT 검증을 위한 공개키를 가져와 캐시하는 컴포넌트입니다.
 * 서버 시작 시 1회 요청하며, 이후 메모리에 저장된 키를 활용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterServerPublicKeyManager {
    private final InterServerKeyProperties interServerKeyProperties;
    private final RestTemplate restTemplate;

    // 서버 간 JWT 검증을 위한 공개키 (메모리 내에 캐싱)
    private RSAPublicKey cachedPublicKey;

    /**
     * 애플리케이션 시작 시, 최초 1회 공개키를 불러옴
     */
    @PostConstruct
    public void init() {
        loadPublicKey();
    }

    /**
     * 외부에서 공개키가 필요할 때 호출
     * 캐시된 키가 없을 경우 즉시 로드
     */
    public RSAPublicKey getPublicKey() {
        if (cachedPublicKey == null) {
            log.warn("공개키 캐시가 비어 있어, 재시도합니다.");
            loadPublicKey();
        }
        if (cachedPublicKey == null) {
            throw new IllegalStateException("공개키를 로드할 수 없습니다. 인증 서버 상태를 확인해주세요.");
        }
        return cachedPublicKey;
    }

    /**
     * 공개키를 Name Server에서 가져와 파싱하고, 캐싱
     */
    private void loadPublicKey(){
        try{
            String endPoint = interServerKeyProperties.getPublicKeyApi();
            log.info("Fetching public key from: {}", endPoint);


            // 최대 3초 지나면 타임아웃 : HttpClientConfig
            ResponseEntity<String> response = restTemplate.getForEntity(endPoint, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String base64Key = response.getBody();
                cachedPublicKey = parsePublicKey(base64Key);
                log.info("공개키 로딩 성공");
            } else {
                log.error("공개키 로딩 실패: 상태 코드 {}", response.getStatusCode());
            }
        } catch (Exception e){
            log.error("공개키 파싱 실패", e);
        }
    }

     /** Base64로 인코딩된 공개키 문자열을 RSAPublicKey 객체로 변환
     * 공개키는 X.509 포맷으로 인코딩된 경우
     */
    private RSAPublicKey parsePublicKey(String base64EncodedKey) throws Exception{
        byte[] keyBytes = Base64.getDecoder().decode(base64EncodedKey);

        // PEM 형식이 아니라 PKCS#8 또는 X.509의 응답인 경우
        java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(keyBytes);
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);

        if (!(publicKey instanceof RSAPublicKey)) {
            throw new IllegalArgumentException("Not and RSA public key");
        }

        return (RSAPublicKey) publicKey;
    }

}
