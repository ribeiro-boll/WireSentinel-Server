package com.bolota.wiresentinelserver.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping(value = {
            "/login",
            "/register",
            "/dashboard",
            "/systems",
            "/systems/{path:[^\\.]*}",
            "/link",
            "/docs"
    })
    public String forward() {
        return "forward:/index.html";
    }
}