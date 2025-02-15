package com.auth.wow.libre.domain.model.security;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.*;

@AllArgsConstructor
public class JwtDto {
    public final String jwt;
    @JsonProperty("refresh_token")
    public final String refreshToken;
    @JsonProperty("expiration_date")
    public final Date expirationDate;
    @JsonProperty("avatar_url")
    public final String avatarUrl;
    public final String language;
}
