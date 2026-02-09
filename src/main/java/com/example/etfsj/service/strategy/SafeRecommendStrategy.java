package com.example.etfsj.service.strategy;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.domain.User;
import com.example.etfsj.dto.RecommendResultDto;
import com.example.etfsj.repository.EtfMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SafeRecommendStrategy implements RecommendStrategy {

    private final EtfMetaRepository etfMetaRepository;

    @Override
    public String getKey() {
        return "SAFE";
    }

    @Override
    public List<RecommendResultDto> recommend(User user) {

        return etfMetaRepository
                .findTop10ByRiskLevelLessThanEqualOrderByRiskLevelAsc(2)
                .stream()
                .map(meta -> new RecommendResultDto(
                        meta.getIsinCd(),
                        meta.getName(),
                        85.0,
                        "위험도가 낮은 ETF 중심 추천",
                        meta.getTheme(),
                        "GREEN"
                ))
                .toList();
    }
}
