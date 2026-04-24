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

                    // 세션에 유저 정보 저장
                    // ※ userId는 반드시 DB의 Long 타입 PK를 저장해야 함
                    // loginId(이메일 문자열)를 저장하면 Long.parseLong() 시 500 에러 발생
                    HttpSession session = request.getSession(true);

                    // OAuth 유저는 loginId = email 로 DB에 저장됨
                    // findLocalUserByLoginId로 email 기준 조회
                    User latestUser = authMapper.findLocalUserByLoginId(loginId);

                    if (latestUser != null) {
                        session.setAttribute("userId",      latestUser.getUserId());      // Long PK
                        session.setAttribute("displayName", latestUser.getDisplayName()); // 닉네임
                    } else {
                        // DB에 없으면 임시로 0 저장 (정상적으로는 saveOrUpdateOAuthUser 후 존재해야 함)
                        session.setAttribute("userId",      0L);
                        session.setAttribute("displayName", loginId);
                        System.out.println("[OAuth2] 경고: DB에서 유저를 찾을 수 없음 - " + loginId);
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