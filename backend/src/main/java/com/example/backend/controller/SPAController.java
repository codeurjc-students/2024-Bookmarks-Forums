package com.example.backend.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SPAController {
    @GetMapping({ "/new/**/{path:[^\\.]*}" })
    public String redirect() {
        return "forward:/";
    }
}