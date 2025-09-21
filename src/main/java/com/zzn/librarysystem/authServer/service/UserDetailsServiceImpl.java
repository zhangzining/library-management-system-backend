package com.zzn.librarysystem.authServer.service;

import com.zzn.librarysystem.userModule.domain.AdminRole;
import com.zzn.librarysystem.userModule.domain.AdminUser;
import com.zzn.librarysystem.userModule.domain.NormalUser;
import com.zzn.librarysystem.userModule.repository.AdminUserRepository;
import com.zzn.librarysystem.userModule.repository.NormalUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

import static com.zzn.librarysystem.common.enums.UserType.ADMIN_USER;
import static com.zzn.librarysystem.common.enums.UserType.NORMAL_USER;
import static com.zzn.librarysystem.common.util.UsernameUtil.getFormatedUsername;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final NormalUserRepository normalUserRepository;
    private final AdminUserRepository adminUserRepository;

    /**
     * 根据用户名查找用户，先找普通用户再找管理员用户
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return normalUserRepository.findByUsername(username)
                .map(this::mapNormalUser)
                .or(() -> adminUserRepository.findByUsername(username).map(this::mapAdminUser))
                .orElseThrow(() -> new OAuth2AuthenticationException("用户不存在"));
    }

    /**
     * 构造管理员用户
     */
    private UserDetails mapAdminUser(AdminUser adminUser) {
        Stream<String> roles = adminUser.getRoles().stream().map(AdminRole::getName);
        List<SimpleGrantedAuthority> grantedAuthorityList = Stream.concat(roles, Stream.of(ADMIN_USER.getAuthority()))
                .map(SimpleGrantedAuthority::new)
                .toList();
        User.UserBuilder userBuilder = User.withUsername(getFormatedUsername(adminUser))
                .password(adminUser.getPassword())
                .authorities(grantedAuthorityList);
        // 处理用户状态
        switch (adminUser.getStatus()) {
            case LOCKED -> userBuilder.accountLocked(true);
            case DISABLED -> userBuilder.disabled(true);
        }
        return userBuilder.build();
    }

    /**
     * 构造普通用户
     */
    private UserDetails mapNormalUser(NormalUser normalUser) {
        User.UserBuilder userBuilder = User.withUsername(getFormatedUsername(normalUser))
                .password(normalUser.getPassword())
                .roles(NORMAL_USER.name());
        // 处理用户状态
        switch (normalUser.getStatus()) {
            case LOCKED -> userBuilder.accountLocked(true);
            case DISABLED -> userBuilder.disabled(true);
        }
        return userBuilder.build();
    }
}
