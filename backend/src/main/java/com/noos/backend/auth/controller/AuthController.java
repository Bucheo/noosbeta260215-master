package com.noos.backend.auth.controller;

import com.noos.backend.auth.dto.SignupRequest;
import com.noos.backend.auth.dto.User;
import com.noos.backend.auth.mapper.AuthMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.noos.backend.auth.service.AuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;
    private final AuthMapper  authMapper;

    public AuthController(AuthService authService, AuthMapper authMapper) {
        this.authService = authService;
        this.authMapper  = authMapper;
    }

    // ── 회원가입 ─────────────────────────────────────────────────────────────
    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest request) {
        try {
            authService.signup(request);
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return "에러: " + e.getMessage();
        }
    }

    // ── 로그인 ───────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public String login(@RequestBody SignupRequest request, HttpSession session) {
        String result = authService.loginAndRole(request);

        System.out.println("[LOGIN] loginId=" + request.getLoginId() + " result=" + result);

        if (!result.equals("fail")) {
            User user = authMapper.findLocalUserByLoginId(request.getLoginId());
            if (user != null) {
                // 세션에 유저 정보 저장
                session.setAttribute("userId",      user.getUserId());
                session.setAttribute("displayName", user.getDisplayName());
                session.setAttribute("role",        result.equals("admin") ? "ADMIN" : "USER");
                session.setMaxInactiveInterval(1800);

                System.out.println("[LOGIN] 세션 저장 완료 - sessionId=" + session.getId()
                    + " userId=" + user.getUserId()
                    + " displayName=" + user.getDisplayName()
                    + " role=" + session.getAttribute("role"));
            } else {
                System.out.println("[LOGIN] 유저를 DB에서 찾을 수 없음");
            }
        }

        return result;
    }

    // ── 세션 확인 ─────────────────────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object userId = session.getAttribute("userId");

        System.out.println("[ME] sessionId=" + session.getId() + " userId=" + userId);

        if (userId == null) {
            return ResponseEntity.ok("notLoggedIn");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("userId",      userId);
        info.put("displayName", session.getAttribute("displayName"));
        info.put("role",        session.getAttribute("role"));
        return ResponseEntity.ok(info);
    }

    // ── 로그아웃 ──────────────────────────────────────────────────────────────
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "ok";
    }
}