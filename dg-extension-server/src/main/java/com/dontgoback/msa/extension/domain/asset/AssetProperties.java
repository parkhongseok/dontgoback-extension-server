package com.dontgoback.msa.extension.domain.asset;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "asset")
public class AssetProperties {
    private Volatility volatility = new Volatility();
    private Clamp clamp = new Clamp();
    private Cache cache = new Cache();
    private Caffeine caffeine = new Caffeine();

    @Getter @Setter
    public static class Volatility {
        /**
         * 로그정규 분포에서 사용할 일일 표준편차 σ
         * 0 이상 값만 허용
         */
        private double sigma = 0.02;
    }

    @Getter @Setter
    public static class Clamp {
        /**
         * 일일 변동률 하한(%). 예: -5.0 이면 최소 -5% 까지
         */
        private double minPercent = -5.0;

        /**
         * 일일 변동률 상한(%). 예: 5.0 이면 최대 +5% 까지
         */
        private double maxPercent = 5.0;
    }

    @Getter @Setter
    public static class Cache {
        /**
         * 날짜 기준 타임존. 캐시 키 생성(LocalDate) 시 사용
         */
        private String zoneId = "Asia/Seoul";
    }

    @Getter @Setter
    public static class Caffeine {
        /**
         * expireAfterWrite 기간(일 단위). 기본 1일
         */
        private int expireAfterWriteDays = 1;

        /**
         * 캐시 최대 엔트리 수. DAU에 맞춰 조정
         */
        private long maxSize = 300_000L;

        /**
         * 캐시 통계 수집 여부
         */
        private boolean recordStats = true;
    }
}
