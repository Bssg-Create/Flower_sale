package com.flower.service;

import com.flower.dto.LoginDto;
import com.flower.dto.UserDto;
import com.flower.entity.User;
import com.flower.vo.LoginVo;

import java.util.List;

public interface UserService {
    LoginVo login(LoginDto loginDto);
    User register(UserDto userDto);
    User getById(Long id);
    List<User> listAll();
    boolean update(User user);
    boolean delete(Long id);
}