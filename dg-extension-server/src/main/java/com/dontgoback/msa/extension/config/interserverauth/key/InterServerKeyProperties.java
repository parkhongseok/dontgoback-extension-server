package com.dontgoback.msa.extension.config.interserverauth.key;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.key")
@Getter
@Setter
public class InterServerKeyProperties {
    private String publicKeyApiUrl;
}
