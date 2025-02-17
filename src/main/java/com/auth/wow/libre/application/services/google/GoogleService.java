package com.auth.wow.libre.application.services.google;

import com.auth.wow.libre.domain.ports.in.google.*;
import com.auth.wow.libre.infrastructure.client.*;
import com.auth.wow.libre.infrastructure.client.dto.*;
import org.springframework.stereotype.*;

@Service
public class GoogleService implements GooglePort {
    private final GoogleClient googleClient;

    public GoogleService(GoogleClient googleClient) {
        this.googleClient = googleClient;
    }

    @Override
    public VerifyCaptchaResponse verifyRecaptcha(VerifyCaptchaRequest request) {
        return googleClient.verifyRecaptcha(request);
    }
}
