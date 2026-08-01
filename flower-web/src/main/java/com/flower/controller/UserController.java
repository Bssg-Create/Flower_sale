package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.config.AuthContext;
import com.flower.entity.User;
import com.flower.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/{id}")
    public ResponseResult<User> getById(@PathVariable Long id, HttpServletRequest request) {
        AuthContext.requireOwnerOrAdmin(request, id);
        return ResponseResult.success(userService.getById(id));
    }

    @GetMapping("/list")
    public ResponseResult<List<User>> listAll(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return ResponseResult.success(userService.listAll());
    }

    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody User user, HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return ResponseResult.success(userService.update(user));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return ResponseResult.success(userService.delete(id));
    }
}
