package com.zzn.librarysystem.authServer.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * 将请求中的 accessToken 转换成 JwtAuthenticationToken 并采用 authorizationService 中存好的 authorities
 */
@Slf4j
public class JwtTokenAuthenticationProvider implements AuthenticationProvider {

    private final OAuth2AuthorizationService authorizationService;
    private final JwtDecoder jwtDecoder;
    private final String principalClaimName = JwtClaimNames.SUB;

    public JwtTokenAuthenticationProvider(OAuth2AuthorizationService authorizationService, JwtDecoder jwtDecoder) {
        this.authorizationService = authorizationService;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 获取 Bearer 头的 JWT token
        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        // 解析成 JWT 对象
        Jwt jwt = getJwt(bearer);

        // 获取登录认证时储存在 authorizationService 中的 authorities 以取代默认 JWT token 中提取的 authorities （默认提取 scope 字段）
        Collection<GrantedAuthority> authorities = Optional.ofNullable(authorizationService.findByToken(bearer.getToken(), OAuth2TokenType.ACCESS_TOKEN))
                .map(o -> o.getAttribute(Principal.class.getName()))
                .map(usernamePasswordAuthenticationToken -> ((UsernamePasswordAuthenticationToken) usernamePasswordAuthenticationToken).getAuthorities())
                .orElse(Collections.emptyList());

        // 组装成 JWT Authentication
        String principalClaimValue = jwt.getClaimAsString(this.principalClaimName);
        return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // 解码 JWT token
    private Jwt getJwt(BearerTokenAuthenticationToken bearer) {
        try {
            return this.jwtDecoder.decode(bearer.getToken());
        }
        catch (BadJwtException failed) {
            log.debug("Failed to authenticate since the JWT was invalid");
            throw new InvalidBearerTokenException(failed.getMessage(), failed);
        }
        catch (JwtException failed) {
            throw new AuthenticationServiceException(failed.getMessage(), failed);
        }
    }
}
