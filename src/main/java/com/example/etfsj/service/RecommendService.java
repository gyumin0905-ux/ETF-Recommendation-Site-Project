package com.example.etfsj.service;

import com.example.etfsj.domain.User;
import com.example.etfsj.dto.RecommendResultDto;
import com.example.etfsj.repository.UserRepository;
import com.example.etfsj.service.strategy.RecommendStrategy;
import com.example.etfsj.service.strategy.ThemePopularRecommendStrategy;
import com.example.etfsj.service.strategy.ThemePopularSafeRecommendStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final UserRepository userRepository;
    private final List<RecommendStrategy> strategies;

    private final Map<String, RecommendStrategy> strategyMap = new HashMap<>();

    /** 전략 초기화: getKey() 기준으로 Map 구성 */
    @PostConstruct
    public void init() {
        for (RecommendStrategy strategy : strategies) {
            strategyMap.put(strategy.getKey(), strategy);
        }
    }

    /** 기존 (theme 없는 경우) */
    public List<RecommendResultDto> recommend(Long userId, String type) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RecommendStrategy strategy =
                strategyMap.getOrDefault(type, strategyMap.get("POPULAR"));

        if (strategy == null) {
            return List.of();
        }

        return strategy.recommend(user);
    }

    /** ⭐ theme 포함 추천 (THEME 계열 전용) */
    public List<RecommendResultDto> recommend(Long userId, String type, String theme) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RecommendStrategy strategy =
                strategyMap.getOrDefault(type, strategyMap.get("POPULAR"));

        // ⭐ THEME + POPULAR
        if (strategy instanceof ThemePopularRecommendStrategy themeStrategy) {
            return themeStrategy.recommendByTheme(user, theme);
        }

        // ⭐ THEME + POPULAR + SAFE
        if (strategy instanceof ThemePopularSafeRecommendStrategy safeStrategy) {
            return safeStrategy.recommendByTheme(user, theme);
        }

        return strategy.recommend(user);
    }
}
