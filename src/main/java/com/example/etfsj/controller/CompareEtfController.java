package com.example.etfsj.controller;

import com.example.etfsj.dto.CompareResultDto;
import com.example.etfsj.dto.CompareReturnChartDto;
import com.example.etfsj.repository.EtfMetaRepository;
import com.example.etfsj.service.CompareEtfService;
import com.example.etfsj.service.CompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/compare")
public class CompareEtfController {

    private final EtfMetaRepository metaRepo;
    private final CompareEtfService compareEtfService;
    private final CompareService compareService;

    /** 1️⃣ 비교 선택 + TOP10 + 랜덤 TOP5 */
    @GetMapping
    public String compareList(Model model) {

        model.addAttribute("etfs", metaRepo.findAll());

        model.addAttribute(
                "randomTop5",
                compareService.compareRandomTop5("RANDOM")
        );

        return "compareList";
    }

    /** 2️⃣ 비교 결과 페이지 */
    @GetMapping("/result")
    public String compareResult(@RequestParam("isin") List<String> isin, Model model) {

        if (isin.size() != 2) {
            throw new IllegalArgumentException("ETF는 2개 선택해야 합니다.");
        }

        model.addAttribute(
                "data",
                compareEtfService.compare(isin.get(0), isin.get(1))
        );

        return "compareEtf";
    }

    /** 3️⃣ 랜덤 1쌍 비교 (기존 유지) */
    @GetMapping("/random")
    public String compareRandom(
            @RequestParam(defaultValue = "RANDOM") String mode,
            Model model
    ) {
        try {
            CompareResultDto result = compareService.compareRandomTheme(mode);
            model.addAttribute("result", result);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "compare";
    }

    /** ✅ 수익률 비교 차트 API */
    @GetMapping("/chart/return")
    @ResponseBody
    public CompareReturnChartDto compareReturnChart(
            @RequestParam String isin1,
            @RequestParam String isin2,
            @RequestParam(defaultValue = "1Y") String period
    ) {
        return compareService.getCompareReturnChart(isin1, isin2, period);
    }
}
