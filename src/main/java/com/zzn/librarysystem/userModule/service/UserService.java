package com.zzn.librarysystem.userModule.service;

import com.zzn.librarysystem.authServer.dto.SimpleUserInfo;
import com.zzn.librarysystem.common.enums.NormalUserStatus;
import com.zzn.librarysystem.common.exception.ApiException;
import com.zzn.librarysystem.common.util.DataUtil;
import com.zzn.librarysystem.userModule.domain.AdminUser;
import com.zzn.librarysystem.userModule.domain.NormalUser;
import com.zzn.librarysystem.userModule.dto.ChangePasswordRequestDto;
import com.zzn.librarysystem.userModule.dto.NormalUserDto;
import com.zzn.librarysystem.userModule.dto.RegisterUserDto;
import com.zzn.librarysystem.userModule.mapper.UserMapper;
import com.zzn.librarysystem.userModule.repository.AdminUserRepository;
import com.zzn.librarysystem.userModule.repository.NormalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.zzn.librarysystem.common.enums.FailedReason.*;
import static com.zzn.librarysystem.common.util.DataUtil.applyIfNotBlank;
import static com.zzn.librarysystem.common.util.DataUtil.applyIfNotNull;
import static com.zzn.librarysystem.common.util.UsernameUtil.ADMIN_PREFIX;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final NormalUserRepository normalUserRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Value("${app.client_id}")
    private String clientId;

    /**
     * 添加普通用户
     */
    public NormalUserDto registerNormalUser(RegisterUserDto registerUserDto) {
        // 如果用户名重复则不允许注册
        if (normalUserRepository.existsByUsername(registerUserDto.getUsername())
                || adminUserRepository.existsByUsername(registerUserDto.getUsername())) {
            throw ApiException.of(USERNAME_EXISTS);
        }

        return userMapper.map(normalUserRepository.save(NormalUser.builder()
                .username(registerUserDto.getUsername())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .clientId(registerUserDto.getClientId())
                .nickname(DataUtil.randomName())
                .status(NormalUserStatus.NEVER_LOGIN)
                .build()), NormalUserDto.class);
    }

    /**
     * 修改用户信息
     */
    public NormalUserDto updateNormalUser(NormalUserDto normalUserDto) {
        NormalUser user = normalUserRepository.findById(normalUserDto.getId())
                .orElseThrow(() -> ApiException.of(USERNAME_NOT_EXISTS));

        applyIfNotBlank(normalUserDto::getEmail, user::setEmail);
        applyIfNotBlank(normalUserDto::getNickname, user::setNickname);
        applyIfNotNull(normalUserDto::getStatus, user::setStatus);

        user = normalUserRepository.save(user);
        return userMapper.map(user, NormalUserDto.class);
    }

    /**
     * 封禁用户
     */
    public void blockNormalUser(Long userId) {
        NormalUser normalUser = normalUserRepository.findById(userId).orElseThrow(() -> ApiException.of(USERNAME_NOT_EXISTS));
        normalUser.setStatus(NormalUserStatus.LOCKED);
        normalUserRepository.save(normalUser);
    }

    /**
     * 初始化 10 个用户
     */
    public void createUsersIfAbsent() {
        if (normalUserRepository.count() == 0) {
            IntStream.range(0, 2).forEach(i -> {
                String username = "USER" + i;
//                String password = String.valueOf(DataUtil.randomNum(10));
                String password = "123456";
                log.warn("==== Init {} user with password: {} ====", username, password);

                normalUserRepository.save(NormalUser.builder()
                        .username(username)
                        .password(passwordEncoder.encode(password))
                        .clientId(clientId)
                        .nickname(DataUtil.randomName())
                        .status(NormalUserStatus.NEVER_LOGIN)
                        .build());
            });
        }
    }

    /**
     * 查询普通用户列表
     */
    public Page<NormalUser> getUsers(Pageable pageable, String username) {
        if (StringUtils.isEmpty(username)) {
            username = "%%";
        } else {
            username = "%" + username + "%";
        }
        return normalUserRepository.findAllByUsernameLike(username, pageable);
    }

    /**
     * 修改密码
     */
    public void changePassword(ChangePasswordRequestDto requestDto) {
        SimpleUserInfo currentUserInfo = DataUtil.getCurrentUserInfo();
        if (Objects.isNull(currentUserInfo)) {
            throw ApiException.of(USERNAME_NOT_EXISTS);
        }
        if (currentUserInfo.getUserType().equals(ADMIN_PREFIX)) {
            adminUserRepository.findByIdAndClientId(currentUserInfo.getId(), requestDto.getClientId())
                    .map(item -> {
                        checkAndSetPassword(item.getPassword(), requestDto, item::setPassword);
                        return item;
                    })
                    .map(adminUserRepository::save)
                    .map(AdminUser::getId);
        } else {
            normalUserRepository.findByIdAndClientId(currentUserInfo.getId(), requestDto.getClientId())
                    .map(item -> {
                        checkAndSetPassword(item.getPassword(), requestDto, item::setPassword);
                        return item;
                    })
                    .map(normalUserRepository::save)
                    .map(NormalUser::getId);
        }
    }

    /**
     * 校验老密码是否匹配，并设置新密码
     */
    private void checkAndSetPassword(String password, ChangePasswordRequestDto requestDto, Consumer<String> setPassword) {
        if (!passwordEncoder.matches(password, requestDto.getOldPassword())) {
            setPassword.accept(passwordEncoder.encode(requestDto.getNewPassword()));
        } else {
            throw ApiException.of(WRONG_PASSWORD);
        }
    }
}
