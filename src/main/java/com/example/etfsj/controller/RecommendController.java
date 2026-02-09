package com.example.etfsj.controller;

import com.example.etfsj.dto.RecommendResultDto;
import com.example.etfsj.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/{userId}")
    public List<RecommendResultDto> recommend(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "POPULAR") String type
    ) {
        return recommendService.recommend(userId, type);
    }
}
