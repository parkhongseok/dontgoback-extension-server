package com.dontgoback.msa.extension.config.interserverauth.client;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "auth.client")
@Getter
public class InterServerAuthClientProperties {
    private String id;
    private String secret;
}