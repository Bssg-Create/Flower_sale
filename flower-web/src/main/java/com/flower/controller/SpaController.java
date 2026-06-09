package com.flower.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {
        "/",
        "/login",
        "/register",
        "/user/**",
        "/admin/**"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}