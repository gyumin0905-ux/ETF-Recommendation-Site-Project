package com.example.etfsj.controller;

import com.example.etfsj.domain.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/myetf")
public class MyEtfController {

    // 1. 개인 맞춤형 추천 페이지
    @GetMapping("/personal")
    public String personal(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        return "myetf/personal"; // templates/myetf/personal.html
    }

    // 2. 포트폴리오 제안 페이지
    @GetMapping("/portfolio")
    public String portfolio(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        return "myetf/portfolio"; // templates/myetf/portfolio.html
    }
}