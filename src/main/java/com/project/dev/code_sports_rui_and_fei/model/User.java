package com.project.dev.code_sports_rui_and_fei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private String email;

    @Column(nullable = false)
    private String avatarEmoji;

    @Column(nullable = false)
    private String gender;

    private Double height;

    private Double targetWeight;

    /** 情侣绑定码，用于配对 */
    private String partnerCode;

    /** 绑定的伴侣用户ID */
    private Long partnerId;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
