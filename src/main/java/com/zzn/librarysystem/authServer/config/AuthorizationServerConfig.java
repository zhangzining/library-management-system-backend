package com.zzn.librarysystem.authServer.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.zzn.librarysystem.authServer.auth.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

import static com.zzn.librarysystem.authServer.OAuth2Constant.GRANT_TYPE_PASSWORD;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true)
public class AuthorizationServerConfig {

    @Value("${app.client_id}")
    private String clientId;

    @Value("${app.raw_client_secret}")
    private String rawClientSecret;

    @Value("${app.access_token_duration_hours:10}")
    private Long accessTokenDuration;

    @Value("${app.refresh_token_duration_hours:60}")
    private Long refreshTokenDuration;

    /**
     * 定义 AuthServer，认证请求会通过该 SecurityFilterChain 处理
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
                                                                      OAuth2AuthorizationService authorizationService,
                                                                      OAuth2TokenGenerator<?> tokenGenerator)
            throws Exception {
        // 放开登陆接口和注册接口
        http.authorizeHttpRequests(authorize ->
                authorize.requestMatchers("/v1/auth/**").permitAll()
        );

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // 设置自定义密码模式对应的 converter 和 provider
                .tokenEndpoint(tokenEndpoint ->
                        tokenEndpoint
                                .accessTokenRequestConverter(
                                        new PasswordGrantAuthenticationConverter())
                                .accessTokenRequestConverter(
                                        new RefreshTokenAuthenticationConverter())
                                .errorResponseHandler(
                                        new AuthErrorAuthenticationFailureHandler())
                                .authenticationProvider(
                                        new PasswordGrantAuthenticationProvider(authorizationService, tokenGenerator)))
                // 开启OpenID Connect 1.0
                .oidc(Customizer.withDefaults());

        http.oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer.jwt(Customizer.withDefaults()));
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * 非认证请求会通过该 SecurityFilterChain 处理并通过携带的 token 获得 Authentication
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, JwtTokenAuthenticationProvider jwtTokenAuthenticationProvider)
            throws Exception {
        // 放行部分接口
        http.authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/assets/**", "/webjars/**", "/login").permitAll()
                                .requestMatchers("/v1/auth/**").permitAll()
                                .requestMatchers("/v1/files/{id}").permitAll()
                                .requestMatchers(HttpMethod.POST, "/v1/users").permitAll()
                                .requestMatchers("/oauth2/**").permitAll()
                                .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults());
        // 设置自定义的 JWT Token Authentication Provider 以正确转换成 Authentication
        http.authenticationProvider(jwtTokenAuthenticationProvider);
        http.oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer.jwt(Customizer.withDefaults())
        );


        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient librarySystemClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientName(clientId)
                .clientId(clientId)
                .clientSecret(passwordEncoder().encode(rawClientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(new AuthorizationGrantType(GRANT_TYPE_PASSWORD))
                .scope("profile")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(accessTokenDuration))
                        .refreshTokenTimeToLive(Duration.ofHours(refreshTokenDuration))
                        .build())
                .build();
        return new InMemoryRegisteredClientRepository(librarySystemClient);
    }


    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * 配置 JWK，为JWT(id_token)提供加密密钥，用于加密/解密或签名/验签
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * 生成RSA密钥对，给上面jwkSource() 方法的提供密钥对
     */
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    /**
     * 配置jwt解析器
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * 配置认证服务器请求地址
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        //什么都不配置，则使用默认地址
        return AuthorizationServerSettings.builder().build();
    }

    /**
     * 配置token生成器
     */
    @Bean
    OAuth2TokenGenerator<?> tokenGenerator(JWKSource<SecurityContext> jwkSource) {
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    JwtTokenAuthenticationProvider jwtTokenAuthenticationProvider(OAuth2AuthorizationService authorizationService, JwtDecoder jwtDecoder) {
        return new JwtTokenAuthenticationProvider(authorizationService, jwtDecoder);
    }
}