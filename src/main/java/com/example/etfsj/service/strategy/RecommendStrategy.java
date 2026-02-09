package com.example.etfsj.service.strategy;

import com.example.etfsj.domain.User;
import com.example.etfsj.dto.RecommendResultDto;

import java.util.List;

public interface RecommendStrategy {

    /** 전략 식별자 (POPULAR, SAFE, THEME 등) */
    String getKey();

    /** 추천 실행 */
    List<RecommendResultDto> recommend(User user);
}
