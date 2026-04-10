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

    public CustomOAuth2User(OAuth2User oAuth2User, String email) {
        this.oAuth2User = oAuth2User;
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

    @Override
    public String getName() {
        return oAuth2User.getName();
    }
}