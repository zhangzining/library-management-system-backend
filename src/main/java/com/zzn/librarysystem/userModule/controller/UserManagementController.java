package com.zzn.librarysystem.userModule.controller;

import com.zzn.librarysystem.bookModule.mapper.Mapper;
import com.zzn.librarysystem.common.dto.PagedResponse;
import com.zzn.librarysystem.common.util.DataUtil;
import com.zzn.librarysystem.userModule.dto.AdminUserDto;
import com.zzn.librarysystem.userModule.dto.NormalUserDto;
import com.zzn.librarysystem.userModule.dto.RegisterUserDto;
import com.zzn.librarysystem.userModule.service.AdminUserService;
import com.zzn.librarysystem.userModule.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/management/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_USER') and hasAnyAuthority('USER_MANAGE')")
@CrossOrigin
public class UserManagementController {

    private final AdminUserService adminUserService;
    private final UserService userService;
    private final Mapper mapper;

    /**
     * 创建管理员用户
     */
    @PostMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ADMIN_USER_MANAGE')")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid RegisterUserDto registerUserDto) {
        registerUserDto.setClientId(DataUtil.getMandatoryClientId());
        adminUserService.registerAdminUser(registerUserDto);
        return ResponseEntity.accepted().build();
    }

    /**
     * 修改管理员信息
     */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN_USER_MANAGE')")
    public ResponseEntity<Void> updateUser(@RequestBody AdminUserDto adminUserDto, @PathVariable Long id) {
        adminUserDto.setId(id);
        adminUserService.updateAdminUser(adminUserDto);
        return ResponseEntity.accepted().build();
    }

    /**
     * 查询管理员列表
     */
    @PreAuthorize("hasAnyAuthority('ADMIN_USER_MANAGE')")
    @GetMapping("/admin")
    public List<AdminUserDto> getAllUsers(@RequestParam(value = "username", required = false) String username) {
        return adminUserService.getAdminUsers(username);
    }

    /**
     * 查询普通用户列表
     */
    @GetMapping("/normal-user")
    public PagedResponse<NormalUserDto> getAllNormalUsers(
            @RequestParam(value = "page", required = false, defaultValue = "0")
            Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10")
            Integer size,
            @RequestParam(value = "username", required = false)
            String username) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("creationTime")));
        return PagedResponse.of(
                userService.getUsers(pageable, username),
                item -> mapper.map(item, NormalUserDto.class)
        );
    }


    /**
     * 封禁普通用户
     */
    @DeleteMapping("/normal-user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.blockNormalUser(id);
        return ResponseEntity.accepted().build();
    }
}
