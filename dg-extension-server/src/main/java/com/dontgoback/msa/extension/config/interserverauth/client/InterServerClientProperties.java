package com.dontgoback.msa.extension.config.interserverauth.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.client")
@Getter
@Setter
public class InterServerClientProperties {
    private String id;
    private String secret;
}