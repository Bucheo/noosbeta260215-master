package com.noos.backend.auth.controller;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.noos.backend.auth.dto.User;
import com.noos.backend.auth.mapper.AdminMapper;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminMapper adminMapper;

    public AdminController(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @GetMapping("/users")
    public List<User> getUserList() {
        return adminMapper.findAllUsers();
    }
}
