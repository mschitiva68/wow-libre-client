package com.auth.wow.libre.infrastructure.client.dto;

import lombok.*;

@Data
public class VerifyCaptchaResponse {
    private Boolean success;
    private String hostname;
}
