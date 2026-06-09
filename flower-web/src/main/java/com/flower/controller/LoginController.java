package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.dto.LoginDto;
import com.flower.dto.UserDto;
import com.flower.entity.User;
import com.flower.service.UserService;
import com.flower.vo.LoginVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginController {
    private final UserService userService;
    public LoginController(UserService userService) { this.userService = userService; }

    @PostMapping("/login")
    public ResponseResult<LoginVo> login(@RequestBody LoginDto loginDto) {
        return ResponseResult.success(userService.login(loginDto));
    }

    @PostMapping("/register")
    public ResponseResult<User> register(@RequestBody UserDto userDto) {
        return ResponseResult.success(userService.register(userDto));
    }
}