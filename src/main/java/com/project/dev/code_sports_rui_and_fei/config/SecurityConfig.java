package com.project.dev.code_sports_rui_and_fei.config;

import com.project.dev.code_sports_rui_and_fei.auth.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 放行: 认证接口
                .requestMatchers("/api/auth/**").permitAll()
                // 放行: 静态资源 (PWA前端)
                .requestMatchers("/", "/index.html", "/css/**", "/js/**",
                        "/manifest.json", "/sw.js", "/icon-180.png",
                        "/favicon.ico", "/*.png", "/*.svg").permitAll()
                // H2 Console (仅开发环境生效)
                .requestMatchers("/h2-console/**").permitAll()
                // 其他API需要认证
                .requestMatchers("/api/**").authenticated()
                // 其余全部放行 (静态资源)
                .anyRequest().permitAll()
            )
            // H2 Console 需要 iframe
            .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
