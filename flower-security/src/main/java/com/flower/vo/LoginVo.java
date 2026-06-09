package com.flower.vo;

import lombok.Data;

@Data
public class LoginVo {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String userType;
    private String token;
}