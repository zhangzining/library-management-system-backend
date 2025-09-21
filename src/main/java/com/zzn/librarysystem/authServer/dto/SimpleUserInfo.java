package com.zzn.librarysystem.authServer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleUserInfo {
    private Long id;
    private String username;
    private String userType;
}
