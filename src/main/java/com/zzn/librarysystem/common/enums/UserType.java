package com.zzn.librarysystem.common.enums;

import lombok.Getter;

@Getter
public enum UserType {
    NORMAL_USER("ROLE_NORMAL_USER"),
    ADMIN_USER("ROLE_ADMIN_USER"),
    ;


    private final String authority;

    UserType(String authority) {
        this.authority = authority;
    }
}
