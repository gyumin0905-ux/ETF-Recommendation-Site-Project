package com.example.etfsj.controller;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.domain.EtfPrice;
import com.example.etfsj.domain.User;
import com.example.etfsj.service.EtfChartService;
import com.example.etfsj.service.EtfCompositionService;
import com.example.etfsj.service.EtfSearchService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final EtfSearchService searchService;
    private final EtfChartService chartService;
    private final EtfCompositionService etfCompositionService;

    // ===============================
    // 🔥 ETF 상세 페이지 (ISIN 기준)
    // ===============================
    // 🔧 [수정] SearchController
// 👉 기존 기능(차트, 북마크, 로그인 주입) 절대 미변경
// 👉 ETF 상세 화면에서 "현재가/등락률/거래대금" 표시용 데이터만 추가

    // ===============================
    // 🔥 ETF 상세 페이지 (ISIN 기준)
    // ===============================
    @GetMapping("/etf/{isin}")
    public String etfDetail(
            @PathVariable String isin,
            HttpSession session,
            Model model
    ) {

        // ===============================
        // ✅ 기존 로그인 유저 처리 (유지)
        // ===============================
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser != null) {
            model.addAttribute("loginUser", loginUser);
        }

        // ===============================
        // ✅ ETF 메타 정보
        // ===============================
        EtfMeta etf = searchService.findByIsin(isin)
                .orElseThrow(() -> new IllegalArgumentException("ETF 없음: " + isin));

        // ===============================
        // 🔧 [추가] 최신 가격 1건
        // ===============================
        EtfPrice price = chartService.findLatestPrice(isin);

        // ===============================
        // 🔧 [추가] 등락률 계산 (VS 기반)
        // ===============================
        Double flucRate = chartService.calcFlucRate(price);

        // ===============================
        // ✅ 기존 카드 데이터 (유지)
        // ===============================
        double returnRate = chartService.calcReturn(isin, "1Y");
        double volatility = chartService.calcVolatility(isin, "1Y") * 100;

        // ===============================
        // Model 주입
        // ===============================

        // 🔥🔥🔥 여기만 추가 🔥🔥🔥
        model.addAttribute("top5", etfCompositionService.generateTop5Composition());

        model.addAttribute("etf", etf);
        model.addAttribute("price", price);           // ⭐ 추가
        model.addAttribute("flucRate", flucRate);     // ⭐ 추가
        model.addAttribute("returnRate", Math.round(returnRate * 100.0) / 100.0);
        model.addAttribute("volatility", Math.round(volatility * 100.0) / 100.0);

        return "etf-detail";
    }

    // ===============================
    // 🔥 가격 차트 API (선형)
    // ===============================
    @ResponseBody
    @GetMapping("/api/etf/chart")
    public Map<String, Object> etfChart(
            @RequestParam String isin,
            @RequestParam String period
    ) {
        Map<String, Object> res = new HashMap<>();

        List<EtfPrice> prices = chartService.getPrices(isin, period);

        res.put("type", "line");
        res.put("labels", prices.stream()
                .map(EtfPrice::getBasDt)
                .toList());
        res.put("data", prices.stream()
                .map(EtfPrice::getClpr)
                .toList());

        return res;
    }
}
