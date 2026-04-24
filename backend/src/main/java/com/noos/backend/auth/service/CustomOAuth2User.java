package com.noos.backend.auth.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final Collection<GrantedAuthority> authorities;
    private final String email; // 이메일을 별도로 저장

    public CustomOAuth2User(OAuth2User oAuth2User, String email) {
        this.oAuth2User = oAuth2User;
        this.email = email; // 이메일 저장

        // 관리자 이메일 여부로 권한 설정
        if ("atfqwe80@gmail.com".equalsIgnoreCase(email)) {
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * getName()을 이메일로 반환
     * 기존: oAuth2User.getName() → 구글 sub 값(숫자 문자열) 반환
     * 수정: email 반환 → SecurityConfig successHandler에서 authentication.getName()으로 이메일 획득 가능
     */
    @Override
    public String getName() {
        return email; // sub 대신 이메일 반환
    }
}