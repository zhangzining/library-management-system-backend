package com.zzn.librarysystem.authServer.controller;

import com.zzn.librarysystem.authServer.dto.AuthRequestDto;
import com.zzn.librarysystem.authServer.dto.AuthResponseDto;
import com.zzn.librarysystem.authServer.service.AuthService;
import com.zzn.librarysystem.common.util.DataUtil;
import com.zzn.librarysystem.userModule.dto.ChangePasswordRequestDto;
import com.zzn.librarysystem.userModule.dto.RegisterUserDto;
import com.zzn.librarysystem.userModule.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@CrossOrigin
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    /**
     * 用户登陆获取token
     */
    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody @Valid AuthRequestDto authRequestDto) {
        authRequestDto.setClientId(DataUtil.getMandatoryClientId());
        return authService.login(authRequestDto);
    }

    /**
     * 刷新用户token
     */
    @PostMapping("/refresh")
    public AuthResponseDto refresh(@RequestBody AuthRequestDto authRequestDto) {
        authRequestDto.setClientId(DataUtil.getMandatoryClientId());
        return authService.refresh(authRequestDto);
    }

    /**
     * 注册普通用户
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserDto registerUserDto) {
        registerUserDto.setClientId(DataUtil.getMandatoryClientId());
        userService.registerNormalUser(registerUserDto);
        return ResponseEntity.accepted().build();
    }

    /**
     * 修改密码
     */
    @PatchMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequestDto requestDto) {
        requestDto.setClientId(DataUtil.getMandatoryClientId());
        userService.changePassword(requestDto);
        return ResponseEntity.accepted().build();
    }
}
