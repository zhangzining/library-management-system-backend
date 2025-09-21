package com.zzn.librarysystem.common.util;

import com.zzn.librarysystem.authServer.dto.SimpleUserInfo;
import com.zzn.librarysystem.common.enums.FailedReason;
import com.zzn.librarysystem.common.exception.ApiException;
import com.zzn.librarysystem.userModule.domain.AdminUser;
import com.zzn.librarysystem.userModule.domain.NormalUser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UsernameUtil {
    private UsernameUtil() {
    }

    public static final String ADMIN_PREFIX = "AM";
    public static final String NORMAL_PREFIX = "NM";

    public static String getFormatedUsername(AdminUser user) {
        return String.format("%s:%s:%s",ADMIN_PREFIX , user.getId(), user.getUsername());
    }

    public static String getFormatedUsername(NormalUser user) {
        return String.format("%s:%s:%s", NORMAL_PREFIX, user.getId(), user.getUsername());
    }

    public static SimpleUserInfo getUserInfoByUsername(String username) {
        String[] sections = username.split(":");
        if (sections.length != 3) {
            log.error("[getUserInfoByUsername] Wrong name:{}", username);
            throw ApiException.of(FailedReason.USERNAME_NOT_VALID);
        }
        return SimpleUserInfo.builder()
                .userType(sections[0])
                .id(Long.parseLong(sections[1]))
                .username(sections[2])
                .build();
    }

}
