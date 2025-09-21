package com.zzn.librarysystem.authServer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String refreshToken;
    private String clientId;
}
