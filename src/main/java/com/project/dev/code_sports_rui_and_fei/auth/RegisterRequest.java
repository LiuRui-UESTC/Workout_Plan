package com.project.dev.code_sports_rui_and_fei.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名3-20个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6个字符")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String email;

    private String gender;

    private String avatarEmoji;

    private Double height;
}
