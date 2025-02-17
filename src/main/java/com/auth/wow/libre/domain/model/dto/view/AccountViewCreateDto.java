package com.auth.wow.libre.domain.model.dto.view;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.*;

@Data
public class AccountViewCreateDto {
    @NotNull
    @Length(min = 5, max = 20)
    private String username;
    @NotNull
    @Length(min = 5, max = 20)
    private String password;
    @NotNull
    @Length(min = 5, max = 40)
    private String email;
}
