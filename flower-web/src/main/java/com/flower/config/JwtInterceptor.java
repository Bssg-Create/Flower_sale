package com.flower.config;

import com.flower.entity.User;
import com.flower.service.UserService;
import com.flower.utils.JwtUtil;
import com.flower.utils.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public JwtInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = WebUtil.getToken(request);
        if (token == null || !JwtUtil.verify(token)) {
            return reject(response, "未登录或token已过期");
        }
        Long userId = JwtUtil.getUserId(token);
        String tokenUserType = JwtUtil.getUserType(token);
        if (userId == null || tokenUserType == null) {
            return reject(response, "登录状态无效");
        }
        User user = userService.getById(userId);
        if (user == null || "0".equals(user.getStatus())) {
            return reject(response, "账号不存在或已被禁用");
        }
        if (!tokenUserType.equals(user.getUserType())) {
            return reject(response, "用户身份已变更，请重新登录");
        }
        request.setAttribute("userId", userId);
        request.setAttribute("userType", user.getUserType());
        return true;
    }

    private boolean reject(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
        return false;
    }
}
