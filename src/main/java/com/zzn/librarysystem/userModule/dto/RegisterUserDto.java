package com.zzn.librarysystem.userModule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class RegisterUserDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String clientId;
    private Set<String> roles = new HashSet<>();
}
