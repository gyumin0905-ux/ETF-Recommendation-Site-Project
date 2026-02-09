package com.example.etfsj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/company")
public class CompanyController {

    // 1. Verte ETF 소개
    @GetMapping("/intro")
    public String intro() {
        return "company/intro";
    }

    // 2. 경영 정보
    @GetMapping("/management")
    public String management() {
        return "company/management";
    }

    // 3. 채용 정보
    @GetMapping("/recruit")
    public String recruit() {
        return "company/recruit";
    }
}