package com.noos.backend.auth.controller;

import com.noos.backend.auth.dto.UpdateUserRequest;
import com.noos.backend.auth.dto.User;
import com.noos.backend.auth.mapper.AdminMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminController(PasswordEncoder passwordEncoder, AdminMapper adminMapper) {
        this.passwordEncoder = passwordEncoder;
        this.adminMapper = adminMapper;
    }

    // 관리자 권한이 있는 사용자만 전체 사용자 목록을 조회할 수 있도록 설정
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = adminMapper.findAllUsers();
        return ResponseEntity.ok(users);
    }

    // 로그인한 사용자의 이메일과 권한 정보
    @GetMapping("/me")
    public ResponseEntity<String> getMe(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null)
            return ResponseEntity.ok("로그인 안됨");
        return ResponseEntity
                .ok("이메일: " + principal.getAttribute("email") + " / 권한: " + principal.getAuthorities().toString());
    }

    // 삭제 기능
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        adminMapper.deleteUser(userId);
        return ResponseEntity.ok("삭제 완료");
    }

    // 수정 기능
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long userId,
            @RequestBody UpdateUserRequest req) {
        try {
            // 비밀번호 있으면 해시 처리
            if (req.getPassword() != null && !req.getPassword().isEmpty()) {
                req.setPassword(passwordEncoder.encode(req.getPassword()));
            }
            adminMapper.updateUser(userId, req);
            return ResponseEntity.ok("수정 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("에러: " + e.getMessage());
        }
    }

    // 검색 기능
    @GetMapping("/search/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<User> users = adminMapper.searchUsers(
                type != null ? type : "",
                keyword != null ? keyword : "",
                startDate != null ? startDate : "",
                endDate != null ? endDate : "");
        return ResponseEntity.ok(users);
    }
}