package com.zzn.librarysystem.userModule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDataMockService implements InitializingBean {

    private final AdminUserService adminUserService;
    private final UserService userService;

    @Override
    public void afterPropertiesSet() {
        adminUserService.createAdminUserIfAbsent();
        userService.createUsersIfAbsent();
    }
}
