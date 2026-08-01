package com.flower.config;

import com.flower.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;

public final class AuthContext {

    private static final String ADMIN = "admin";

    private AuthContext() {
    }

    public static Long getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (!(userId instanceof Long)) {
            throw new BaseException(401, "未登录或登录状态已失效");
        }
        return (Long) userId;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return ADMIN.equals(request.getAttribute("userType"));
    }

    public static void requireAdmin(HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BaseException(403, "无管理员权限");
        }
    }

    public static void requireOwnerOrAdmin(HttpServletRequest request, Long ownerId) {
        if (!isAdmin(request) && !Objects.equals(getUserId(request), ownerId)) {
            throw new BaseException(403, "无权访问该资源");
        }
    }

    public static void requireOwner(HttpServletRequest request, Long ownerId) {
        if (!Objects.equals(getUserId(request), ownerId)) {
            throw new BaseException(403, "无权操作该资源");
        }
    }
}
