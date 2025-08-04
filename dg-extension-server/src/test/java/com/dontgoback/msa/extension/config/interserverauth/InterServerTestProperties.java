package com.dontgoback.msa.extension.config.interserverauth;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class InterServerTestProperties {
    private Client client = new Client();
    private Key key = new Key();
    private Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class Client {
        private String id;
        private String secret;
    }

    @Getter
    @Setter
    public static class Key {
        private String publicKeyApi;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String issuer;
        private String jwtApi;
    }

    @PostConstruct
    public void debugProperties() {
        System.out.println("✅ InterServerTestProperties 로딩 성공");
        System.out.println("  client.id = " + client.getId());
        System.out.println("  client.secret = " + client.getSecret());
        System.out.println("  jwt.issuer = " + jwt.getIssuer());
        System.out.println("  jwt.jwtApi = " + jwt.getJwtApi());
        System.out.println("  key.publicKeyApi = " + key.getPublicKeyApi());
    }
}
