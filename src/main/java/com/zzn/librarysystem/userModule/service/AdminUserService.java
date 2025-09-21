package com.zzn.librarysystem.userModule.service;

import com.zzn.librarysystem.common.enums.AdminUserStatus;
import com.zzn.librarysystem.common.exception.ApiException;
import com.zzn.librarysystem.userModule.domain.AdminRole;
import com.zzn.librarysystem.userModule.domain.AdminUser;
import com.zzn.librarysystem.userModule.dto.AdminUserDto;
import com.zzn.librarysystem.userModule.dto.RegisterUserDto;
import com.zzn.librarysystem.userModule.mapper.UserMapper;
import com.zzn.librarysystem.userModule.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zzn.librarysystem.common.enums.FailedReason.USERNAME_EXISTS;
import static com.zzn.librarysystem.common.enums.FailedReason.USERNAME_NOT_EXISTS;
import static com.zzn.librarysystem.common.util.DataUtil.applyIfNotNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserService {
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Value("${app.client_id}")
    private String clientId;

    private static final String ADMIN_USERNAME = "admin";

    /**
     * 添加管理员用户
     */
    public void registerAdminUser(RegisterUserDto registerUserDto) {
        if (adminUserRepository.existsByUsername(registerUserDto.getUsername())) {
            throw ApiException.of(USERNAME_EXISTS);
        }

        adminUserRepository.save(AdminUser.builder()
                .username(registerUserDto.getUsername())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .clientId(registerUserDto.getClientId())
                .roles(registerUserDto.getRoles().stream().map(AdminRole::new).collect(Collectors.toSet()))
                .status(AdminUserStatus.LOCKED)
                .build());
    }

    /**
     * 修改管理员角色
     */
    public void updateAdminUser(AdminUserDto adminUserDto) {
        // ADMIN不允许修改
        AdminUser user = adminUserRepository.findById(adminUserDto.getId())
                .filter(adminUser -> !adminUser.getUsername().equals(ADMIN_USERNAME))
                .orElseThrow(() -> ApiException.of(USERNAME_NOT_EXISTS));

        if (CollectionUtils.isNotEmpty(adminUserDto.getRoles())) {
            user.setRoles(adminUserDto.getRoles().stream().map(AdminRole::new).collect(Collectors.toSet()));
        }
        applyIfNotNull(adminUserDto::getStatus, user::setStatus);

        adminUserRepository.save(user);
    }

    /**
     * 获取管理员列表
     */
    public List<AdminUserDto> getAdminUsers(String username) {
        if (StringUtils.isEmpty(username)) {
            username = "%%";
        } else {
            username = "%" + username + "%";
        }
        return userMapper.mapAsList(adminUserRepository.findAllByUsernameLike(username), AdminUserDto.class);
    }

    /**
     * 初始化 ADMIN 用户
     */
    public void createAdminUserIfAbsent() {
        if (adminUserRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
            String password = "123456";
//            String password = String.valueOf(DataUtil.randomNum(10));
            log.warn("==== Init admin user with password: {} ====", password);

            adminUserRepository.save(AdminUser.builder()
                    .username(ADMIN_USERNAME)
                    .password(passwordEncoder.encode(password))
                    .status(AdminUserStatus.ACTIVE)
                    .roles(Set.of(
                            new AdminRole("ADMIN"),
                            new AdminRole("LENDING_MANAGE"),
                            new AdminRole("BOOK_MANAGE"),
                            new AdminRole("USER_MANAGE")
                    ))
                    .clientId(clientId)
                    .build());

            adminUserRepository.save(AdminUser.builder()
                    .username("lending_admin")
                    .password(passwordEncoder.encode(password))
                    .status(AdminUserStatus.ACTIVE)
                    .roles(Set.of(
                            new AdminRole("ADMIN"),
                            new AdminRole("LENDING_MANAGE")
                    ))
                    .clientId(clientId)
                    .build());
        }
    }
}
