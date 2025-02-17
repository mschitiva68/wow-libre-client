package com.auth.wow.libre.domain.ports.in.google;

import com.auth.wow.libre.infrastructure.client.dto.*;

public interface GooglePort {
    VerifyCaptchaResponse verifyRecaptcha(VerifyCaptchaRequest request);
}
