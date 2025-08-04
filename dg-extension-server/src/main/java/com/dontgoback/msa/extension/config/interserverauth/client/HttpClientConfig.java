package com.dontgoback.msa.extension.config.interserverauth.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {
    // frontend 의 fetch처럼 해당 서버의 "요청"을 보내기 위한 도구를 다른 곳에서도 쓸 수 있도록 등록
    @Bean
    public RestTemplate restTemplate() {

        /**
         *  인증 서버가 다운되면, 확장 서버의 요청 쓰레드가 무기한 대기 → 전체 서비스 지연 가능성
         *  지연 방지를 위한 타임아웃 설정
         */
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        return new RestTemplate(factory);
    }
}
