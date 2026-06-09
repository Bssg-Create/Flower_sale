package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.entity.User;
import com.flower.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/{id}")
    public ResponseResult<User> getById(@PathVariable Long id) { return ResponseResult.success(userService.getById(id)); }

    @GetMapping("/list")
    public ResponseResult<List<User>> listAll() { return ResponseResult.success(userService.listAll()); }

    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody User user) { return ResponseResult.success(userService.update(user)); }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) { return ResponseResult.success(userService.delete(id)); }
}