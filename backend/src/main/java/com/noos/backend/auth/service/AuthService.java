package com.noos.backend.auth.service;

import com.noos.backend.auth.dto.SignupRequest;
import com.noos.backend.auth.dto.User;
import com.noos.backend.auth.mapper.AuthMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;

@Service
public class AuthService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthMapper authMapper, PasswordEncoder passwordEncoder) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입 시 비밀번호를 해시해서 저장
    @Transactional
    public void signup(SignupRequest request) {
        request.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        authMapper.insertUser(request);
    }

    // 로그인 시 입력 비밀번호와 저장된 해시를 비교
    public boolean login(SignupRequest request) {
        User user = authMapper.findLocalUserByLoginId(request.getLoginId());
        if (user == null || user.getPasswordHash() == null) {
            return false;
        }

        return passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
    }

    // 관리자 여부를 포함한 로그인 결과 반환
    // - returns "admin" when credentials valid and loginId equals 'admin'
    // - returns "ok" when credentials valid for regular user
    // - returns "fail" when credentials invalid
    public String loginAndRole(SignupRequest request) {
        User user = authMapper.findLocalUserByLoginId(request.getLoginId());
        if (user == null || user.getPasswordHash() == null) {
            return "fail";
        }

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!matches) {
            return "fail";
        }

        // 현재는 간단히 loginId가 'atfqwe80@gmail.com'인 계정을 관리자 처리합니다.
        // 향후에는 users 테이블에 role 컬럼을 추가하는 것이 더 권장됩니다.
        String role;
        if ("atfqwe80@gmail.com".equalsIgnoreCase(user.getLoginId())) {
            role = "admin";
        } else {
            role = "ok";
        }

        // Spring Security 인증 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getLoginId(),
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (role.equals("admin") ? "ADMIN" : "USER")))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return role;
    }
}
