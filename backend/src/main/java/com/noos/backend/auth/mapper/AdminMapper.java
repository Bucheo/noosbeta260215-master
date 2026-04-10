package com.noos.backend.auth.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.noos.backend.auth.dto.UpdateUserRequest;
import com.noos.backend.auth.dto.User;

@Mapper
public interface AdminMapper {
    List<User> findAllUsers();

    //사용자 삭제 기능
    void deleteUser(Long userId); 

    //사용자 정보 수정 기능
    void updateUser(@Param("userId") Long userId, @Param("req") UpdateUserRequest req);
}
