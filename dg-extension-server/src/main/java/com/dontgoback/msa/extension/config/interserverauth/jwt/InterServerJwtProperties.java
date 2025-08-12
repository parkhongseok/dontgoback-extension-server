package com.dontgoback.msa.extension.config.interserverauth.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.jwt")
@Getter
@Setter
public class InterServerJwtProperties {
    private String issuer;
}
