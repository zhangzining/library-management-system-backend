package com.zzn.librarysystem.userModule.dto;

import com.zzn.librarysystem.common.enums.NormalUserStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class NormalUserDto {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String clientId;
    private NormalUserStatus status;
    private Instant lastLoginTime;
}
