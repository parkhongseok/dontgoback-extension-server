package com.dontgoback.msa.extension.config.interserverauth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class InterServerKeyTokenTestProvider {

    @Autowired
    private InterServerTestProperties props;

    public String getToken() throws Exception {
        URL url = new URL(props.getJwt().getJwtApi());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonPayload = """
        {
            "clientId": "%s",
            "clientSecret": "%s"
        }
        """.formatted(props.getClient().getId(), props.getClient().getSecret());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public RSAPublicKey getPublicKey() throws Exception {
        URL url = new URL(props.getKey().getPublicKeyApi());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (InputStream is = conn.getInputStream()) {
            String base64 = new String(is.readAllBytes());
            byte[] keyBytes = Base64.getDecoder().decode(base64);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(keyBytes));
        }
    }
}
