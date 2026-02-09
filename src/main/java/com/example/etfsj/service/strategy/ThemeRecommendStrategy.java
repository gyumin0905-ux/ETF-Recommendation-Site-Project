package com.example.etfsj.service.strategy;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.domain.User;
import com.example.etfsj.dto.RecommendResultDto;
import com.example.etfsj.repository.EtfMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThemeRecommendStrategy implements RecommendStrategy {

    private final EtfMetaRepository etfMetaRepository;

    @Override
    public String getKey() {
        return "THEME";
    }

    @Override
    public List<RecommendResultDto> recommend(User user) {

        // 1️⃣ 테마 있는 ETF만
        List<EtfMeta> all = etfMetaRepository.findAll().stream()
                .filter(m -> m.getTheme() != null && !m.getTheme().isBlank())
                .toList();

        // 2️⃣ 테마별 그룹핑
        Map<String, List<EtfMeta>> byTheme =
                all.stream().collect(Collectors.groupingBy(EtfMeta::getTheme));

        // 3️⃣ 각 테마에서 대표 ETF 1~2개씩 추출
        return byTheme.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .sorted(Comparator.comparingInt(EtfMeta::getRiskLevel)) // 안정적인 ETF 우선
                        .limit(2)
                        .map(meta -> new RecommendResultDto(
                                meta.getIsinCd(),
                                meta.getName(),
                                80, // 테마 추천은 고정 점수
                                "테마별 대표 ETF 추천",
                                meta.getTheme(),
                                "PURPLE"
                        ))
                )
                .limit(20)
                .toList();
    }
}
