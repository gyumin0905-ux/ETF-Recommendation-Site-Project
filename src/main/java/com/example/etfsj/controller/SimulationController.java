package com.example.etfsj.controller;

import com.example.etfsj.domain.*;
import com.example.etfsj.service.EtfSearchService;
import com.example.etfsj.service.SimulationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SimulationController {

    private final EtfSearchService etfSearchService;
    private final SimulationService simulationService;

    // ===============================
    // 시뮬레이터 진입 (입력만)
    // ===============================
    @GetMapping("/simulate/{isin}")
    public String runPage(
            @PathVariable String isin,
            Model model
    ) {
        EtfMeta etf = etfSearchService.findByIsin(isin)
                .orElseThrow(() -> new IllegalArgumentException("ETF 없음"));

        model.addAttribute("etf", etf);
        return "simulate"; // ✅ 단일 HTML
    }

    // ===============================
    // 시뮬레이션 실행 (결과 포함)
    // ===============================
    @PostMapping("/simulate/{isin}/run")
    @ResponseBody
    public SimulationResult runSimulationAjax(
            @PathVariable String isin,
            @RequestParam int monthlyAmount,
            @RequestParam String startYm,
            @RequestParam String endYm,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute("loginUser");

        return simulationService.simulate(
                isin,
                monthlyAmount,
                startYm,
                endYm,
                loginUser
        );
    }
}
