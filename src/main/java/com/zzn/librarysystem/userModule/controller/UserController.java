package com.zzn.librarysystem.userModule.controller;

import com.zzn.librarysystem.common.util.DataUtil;
import com.zzn.librarysystem.userModule.dto.NormalUserDto;
import com.zzn.librarysystem.userModule.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('NORMAL_USER')")
@CrossOrigin
public class UserController {

    private final UserService userService;

    /**
     * 修改用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('NORMAL_USER')")
    public NormalUserDto updateUser(@RequestBody @Valid NormalUserDto normalUserDto, @PathVariable Long id) {
        normalUserDto.setId(DataUtil.getCurrentUserId());
        return userService.updateNormalUser(normalUserDto);
    }
}
