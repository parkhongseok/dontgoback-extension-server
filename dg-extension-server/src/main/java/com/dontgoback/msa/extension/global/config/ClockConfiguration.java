package com.dontgoback.msa.extension.global.config;

import com.dontgoback.msa.extension.domain.asset.AssetProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfiguration {

    @Bean
    public Clock clock(AssetProperties assetProperties) {
        ZoneId zoneId = ZoneId.of(assetProperties.getCache().getZoneId());
        return Clock.system(zoneId);
    }
}