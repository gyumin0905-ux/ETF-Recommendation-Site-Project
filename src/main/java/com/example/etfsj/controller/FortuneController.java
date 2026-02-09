package com.example.etfsj.controller;

import com.example.etfsj.dto.FortuneResponseDto;
import com.example.etfsj.service.FortuneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class FortuneController {

    private final FortuneService fortuneService;

    // 1. 별자리 선택 화면 보여주기
    @GetMapping("/fortune")
    public String fortuneIndex() {
        return "fortune/index";
    }

    // 2. 선택 후 결과 화면 보여주기 (크롤링 + 추천 실행)
    @GetMapping("/fortune/result")
    public String fortuneResult(@RequestParam("sign") String sign, Model model) {
        // 서비스에서 크롤링 및 추천 결과 받아오기
        FortuneResponseDto result = fortuneService.getFortuneAndRecommend(sign);

        // 화면(HTML)으로 데이터 전달
        model.addAttribute("result", result);

        return "fortune/result";
    }
}