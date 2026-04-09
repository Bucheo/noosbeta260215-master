package com.noos.backend.auth.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.noos.backend.auth.dto.User;

@Mapper
public interface AdminMapper {
    List<User> findAllUsers();
}
