package com.noos.backend.auth.controller;

import com.noos.backend.auth.dto.SignupRequest;
import org.springframework.web.bind.annotation.*;
import com.noos.backend.auth.service.AuthService;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 회원가입 요청
    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest request) {
        try {
            authService.signup(request);
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return "에러: " + e.getMessage(); // 에러 내용 프론트로 반환
        }
    }

    // 로그인 요청
    @PostMapping("/login")
    public String login(@RequestBody SignupRequest request) {
        String result = authService.loginAndRole(request);
        // 반환값: "admin" | "ok" | "fail"
        return result;
    }
}