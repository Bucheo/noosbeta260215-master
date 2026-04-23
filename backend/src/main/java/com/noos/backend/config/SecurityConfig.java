package com.noos.backend.config;

import com.noos.backend.auth.mapper.AuthMapper;
import com.noos.backend.auth.dto.User;
import com.noos.backend.auth.service.CustomOAuth2UserService;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final AuthMapper authMapper; // OAuth2 로그인 시 세션 저장에 사용

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          AuthMapper authMapper) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.authMapper = authMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 세션 유지 설정 (IF_REQUIRED: 필요 시 생성, 생성 후 유지)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**", "/login/**", "/signup/**",
                    "/oauth2/**", "/ws/**", "/api/chat/**"
                ).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                // OAuth2 로그인 성공 시 세션에 userId / displayName / role 저장
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                    // 로그인한 유저의 이메일(loginId) 추출
                    String loginId = authentication.getName();

                    // DB에서 유저 정보 조회
                    User user = authMapper.findLocalUserByLoginId(loginId);

                    // OAuth2 전용 조회 (provider = google/github)
                    if (user == null) {
                        // OAuth 계정은 loginId = email 로 저장되므로 재시도
                        try {
                            user = authMapper.findLocalUserByLoginId(loginId);
                        } catch (Exception e) {
                            System.out.println("[OAuth2] 유저 조회 실패: " + e.getMessage());
                        }
                    }

                    // 세션에 유저 정보 저장
                    HttpSession session = request.getSession(true);
                    if (user != null) {
                        session.setAttribute("userId",      user.getUserId());
                        session.setAttribute("displayName", user.getDisplayName());
                    } else {
                        // DB 조회 실패 시 authentication에서 직접 추출
                        session.setAttribute("userId",      loginId);
                        session.setAttribute("displayName", loginId);
                    }
                    session.setAttribute("role", isAdmin ? "ADMIN" : "USER");
                    session.setMaxInactiveInterval(1800); // 30분

                    System.out.println("[OAuth2] 세션 저장 완료 - loginId=" + loginId
                        + " role=" + session.getAttribute("role")
                        + " sessionId=" + session.getId());

                    // 관리자 → /admin, 일반 유저 → 홈
                    if (isAdmin) {
                        response.sendRedirect("http://localhost:3000/admin");
                    } else {
                        response.sendRedirect("http://localhost:3000");
                    }
                })
                .failureUrl("/login?error=true")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // 세션 쿠키 전달 필수
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}