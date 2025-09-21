package com.zzn.librarysystem.authServer.service;

import com.zzn.librarysystem.authServer.dto.AuthRequestDto;
import com.zzn.librarysystem.authServer.dto.AuthResponseDto;
import com.zzn.librarysystem.authServer.dto.AuthServerResponseDto;
import com.zzn.librarysystem.common.enums.AdminUserStatus;
import com.zzn.librarysystem.common.enums.NormalUserStatus;
import com.zzn.librarysystem.common.exception.ApiException;
import com.zzn.librarysystem.userModule.dto.AdminUserDto;
import com.zzn.librarysystem.userModule.dto.NormalUserDto;
import com.zzn.librarysystem.userModule.mapper.UserMapper;
import com.zzn.librarysystem.userModule.repository.AdminUserRepository;
import com.zzn.librarysystem.userModule.repository.NormalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import static com.zzn.librarysystem.common.enums.FailedReason.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final RestTemplate restTemplate;
    private final NormalUserRepository normalUserRepository;
    private final AdminUserRepository adminUserRepository;
    private final UserMapper userMapper;

    @Value("${app.client_id}")
    private String clientId;

    @Value("${app.raw_client_secret}")
    private String raw_client_secret;

    @Value("${server.servlet.context-path}")
    private String pathPrefix;

    @Value("${server.port}")
    private Integer port;


    private RequestEntity<Void> getRequestEntity(AuthRequestDto requestDto, Supplier<String> urlGenerator) {
        HttpHeaders headers = new HttpHeaders();
        Optional.ofNullable(requestDto.getClientId())
                .filter(clientId::equals)
                .map(id -> {
                    headers.setBasicAuth(clientId, raw_client_secret);
                    return id;
                })
                .orElseThrow(() -> ApiException.of(CLIENT_ID_NOT_FOUND));

        return new RequestEntity<>(headers, HttpMethod.POST, URI.create(urlGenerator.get()));
    }

    private String getAuthServerPrefix() {
        return String.format("http://localhost:%d%s", port, pathPrefix);
    }

    /**
     * 代理登录方法，通过请求 authServer的 token 接口生成 token
     */
    public AuthResponseDto login(AuthRequestDto requestDto) {
        Supplier<String> urlGenerator = () ->
             String.format("%s/oauth2/token?grant_type=authorization_password&scope=profile&username=%s&password=%s",
                     getAuthServerPrefix(),
                     requestDto.getUsername(),
                     requestDto.getPassword());
        AuthResponseDto authResponseDto = exchangeForToken(requestDto, urlGenerator);
        // 将用户信息一并返回
        authResponseDto.setNormalUser(getAndActivateNormalUser(requestDto.getUsername()));
        authResponseDto.setAdminUser(getAndActivateAdminUser(requestDto.getUsername()));

        return authResponseDto;
    }

    /**
     * 代理刷新 token 方法，通过请求 authServer的 token 接口生成新的 token
     */
    public AuthResponseDto refresh(AuthRequestDto requestDto) {
        Supplier<String> urlGenerator = () ->
                String.format("%s/oauth2/token?grant_type=refresh_token&refresh_token=%s",
                        getAuthServerPrefix(),
                        requestDto.getRefreshToken());
        AuthResponseDto authResponseDto = exchangeForToken(requestDto, urlGenerator);
        // 将用户信息一并返回
        authResponseDto.setNormalUser(getAndActivateNormalUser(requestDto.getUsername()));
        authResponseDto.setAdminUser(getAndActivateAdminUser(requestDto.getUsername()));

        return authResponseDto;
    }

    private AuthResponseDto exchangeForToken(AuthRequestDto requestDto, Supplier<String> urlGenerator) {
        try {
            ResponseEntity<AuthServerResponseDto> responseEntity = restTemplate.exchange(getRequestEntity(requestDto, urlGenerator), AuthServerResponseDto.class);
            return Optional.ofNullable(responseEntity.getBody())
                    .map(resp -> AuthResponseDto.builder()
                            .accessToken(resp.getAccessToken())
                            .refreshToken(resp.getRefreshToken())
                            .build())
                    .orElseThrow(() -> ApiException.of(REQUEST_LOGIN_FAILED));
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("AUTH_FAILED", e);
            throw ApiException.builder()
                    .rawJson(e.getResponseBodyAsString())
                    .httpStatus(e.getStatusCode())
                    .build();
        } catch (HttpStatusCodeException e) {
            log.error("HTTP_STATUS_ERROR", e);
            throw new ApiException(AUTH_FAILED.name(), e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            log.error("REQUEST_LOGIN_FAILED", e);
            throw ApiException.of(AUTH_FAILED);
        }
    }

    private NormalUserDto getAndActivateNormalUser(String username) {
        return normalUserRepository.findByUsername(username)
                .map(item -> {
                    if (item.getStatus() == NormalUserStatus.LOCKED) {
                        throw ApiException.of(USERNAME_LOCKED);
                    }
                    item.setStatus(NormalUserStatus.ACTIVE);
                    item.setLastLoginTime(Instant.now());
                    return item;
                })
                .map(normalUserRepository::save)
                .map(item -> userMapper.map(item, NormalUserDto.class))
                .orElse(null);
    }

    private AdminUserDto getAndActivateAdminUser(String username) {
        return adminUserRepository.findByUsername(username)
                .map(item -> {
                    if (item.getStatus() == AdminUserStatus.LOCKED) {
                        throw ApiException.of(USERNAME_LOCKED);
                    }
                    item.setLastLoginTime(Instant.now());
                    return item;
                })
                .map(adminUserRepository::save)
                .map(item -> userMapper.map(item, AdminUserDto.class))
                .orElse(null);
    }
}
