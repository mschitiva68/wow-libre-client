package com.auth.wow.libre.infrastructure.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class Configurations {
    @Value("${application.urls.wow-libre-login}")
    private String pathLoginWowLibre;
    @Value("${application.urls.wow-libre-server}")
    private String pathServerWowLibre;
    @Value("${application.account.username:default}")
    private String loginUsername;
    @Value("${application.account.password:default}")
    private String loginPassword;
    @Value("${application.account.api-key:default}")
    private String serverApiKey;
    @Value("${events.twinks.enable}")
    private boolean eventTwink;
    @Value("${google.api-secret}")
    private String apiSecret;
    @Value("${google.api-key}")
    private String apiKey;
    @Value("${server-web.name:Wow Private Server}")
    private String serverWebName;
}
