package com.zzn.librarysystem.authServer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zzn.librarysystem.userModule.dto.AdminUserDto;
import com.zzn.librarysystem.userModule.dto.NormalUserDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private NormalUserDto normalUser;
    private AdminUserDto adminUser;
}
