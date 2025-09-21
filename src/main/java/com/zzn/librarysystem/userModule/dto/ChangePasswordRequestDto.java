package com.zzn.librarysystem.userModule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    private String clientId;
    private Long id;
}
