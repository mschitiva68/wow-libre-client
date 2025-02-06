package com.auth.wow.libre.domain.model.dto;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

public class LoginResponseDto {
    public String jwt;
    @JsonProperty("refresh_token")
    public String refreshToken;
    @JsonProperty("expiration_date")
    public Date expirationDate;

}
