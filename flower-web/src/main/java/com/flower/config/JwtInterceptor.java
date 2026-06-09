package com.flower.config;

import com.flower.utils.JwtUtil;
import com.flower.utils.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = WebUtil.getToken(request);
        if (token == null || !JwtUtil.verify(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\"}");
            return false;
        }
        Long userId = JwtUtil.getUserId(token);
        String userType = JwtUtil.getUserType(token);
        request.setAttribute("userId", userId);
        request.setAttribute("userType", userType);
        return true;
    }
}