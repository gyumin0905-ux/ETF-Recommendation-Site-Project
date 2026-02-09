package com.example.etfsj.controller;

import com.example.etfsj.dto.RecommendResultDto;
import com.example.etfsj.repository.EtfMetaRepository;
import com.example.etfsj.service.CompareService;
import com.example.etfsj.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RecommendViewController {

    private final RecommendService recommendService;
    private final EtfMetaRepository etfMetaRepository;

    @GetMapping("/recommend/{userId}")
    public String recommendPage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "POPULAR") String type,
            @RequestParam(required = false) String theme,
            Model model
    ) {
        List<RecommendResultDto> results =
                recommendService.recommend(userId, type, theme);

        // ⭐ THEME_POPULAR / THEME_POPULAR_SAFE 모두 테마 선택 단계 포함
        if ("THEME_POPULAR".equals(type) || "THEME_POPULAR_SAFE".equals(type)) {
            model.addAttribute("themes", etfMetaRepository.findDistinctThemes());
        }

        model.addAttribute("results", results);
        model.addAttribute("type", type);
        model.addAttribute("theme", theme);
        model.addAttribute("userId", userId);
        return "recommend";
    }
}
