package com.project.dev.code_sports_rui_and_fei.auth;

import com.project.dev.code_sports_rui_and_fei.model.User;
import com.project.dev.code_sports_rui_and_fei.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名已存在"));
        }

        String emoji = req.getAvatarEmoji() != null ? req.getAvatarEmoji() : "💪";
        String gender = req.getGender() != null ? req.getGender() : "other";

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .email(req.getEmail())
                .avatarEmoji(emoji)
                .gender(gender)
                .height(req.getHeight())
                .partnerCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getNickname(), user.getAvatarEmoji()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElse(null);
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getNickname(), user.getAvatarEmoji()));
    }

    /** 绑定情侣 */
    @PostMapping("/bind-partner")
    public ResponseEntity<?> bindPartner(@RequestAttribute("userId") Long userId,
                                          @RequestBody Map<String, String> body) {
        String code = body.get("partnerCode");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请输入伴侣绑定码"));
        }

        User partner = userRepository.findByPartnerCode(code.toUpperCase()).orElse(null);
        if (partner == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "绑定码无效"));
        }
        if (partner.getId().equals(userId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "不能绑定自己"));
        }

        User me = userRepository.findById(userId).orElseThrow();
        me.setPartnerId(partner.getId());
        partner.setPartnerId(me.getId());
        userRepository.save(me);
        userRepository.save(partner);

        return ResponseEntity.ok(Map.of("message", "绑定成功! 你的伴侣: " + partner.getNickname()));
    }
}
