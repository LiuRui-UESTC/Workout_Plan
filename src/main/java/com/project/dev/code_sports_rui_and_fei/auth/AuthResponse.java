package com.project.dev.code_sports_rui_and_fei.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String nickname;
    private String avatarEmoji;
}
