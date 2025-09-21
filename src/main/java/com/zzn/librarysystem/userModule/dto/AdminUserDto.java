package com.zzn.librarysystem.userModule.dto;

import com.zzn.librarysystem.common.enums.AdminUserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class AdminUserDto {
    @NotNull
    private Long id;
    private String username;
    private String clientId;
    private AdminUserStatus status;
    private Instant lastLoginTime;
    private Set<String> roles;
}
