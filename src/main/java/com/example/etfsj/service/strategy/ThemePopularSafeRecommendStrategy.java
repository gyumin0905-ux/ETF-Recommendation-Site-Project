package com.example.etfsj.service.strategy;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.domain.User;
import com.example.etfsj.dto.RecommendResultDto;
import com.example.etfsj.repository.EtfMetaRepository;
import com.example.etfsj.repository.EtfPriceRepository;
import com.example.etfsj.util.PeriodUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThemePopularSafeRecommendStrategy implements RecommendStrategy {

    private final EtfPriceRepository etfPriceRepository;
    private final EtfMetaRepository etfMetaRepository;

    /** ⭐ URL type 과 반드시 일치 */
    @Override
    public String getKey() {
        return "THEME_POPULAR_SAFE";
    }

    @Override
    public List<RecommendResultDto> recommend(User user) {
        // theme 없으면 의미 없으므로 빈 리스트
        return List.of();
    }

    /** ⭐ 핵심: 테마 + 인기 + SAFE */
    public List<RecommendResultDto> recommendByTheme(User user, String theme) {

        String fromDt = PeriodUtil.daysAgo(30);

        // 1️⃣ 인기 데이터
        List<Object[]> rows = etfPriceRepository.findAvgTradeAmount(fromDt);
        if (rows.isEmpty()) return List.of();

        // 2️⃣ 메타 정보
        Map<String, EtfMeta> metaMap =
                etfMetaRepository.findAll().stream()
                        .filter(m -> m.getTheme() != null)
                        .collect(Collectors.toMap(
                                EtfMeta::getIsinCd,
                                m -> m
                        ));

        // 3️⃣ 테마 + SAFE 필터
        List<Object[]> filtered = rows.stream()
                .filter(r -> {
                    EtfMeta meta = metaMap.get((String) r[0]);
                    if (meta == null) return false;

                    // 테마 필터
                    if (theme == null || !meta.getTheme().contains(theme)) {
                        return false;
                    }

                    // SAFE 조건
                    // 1) 위험도 2 이하
                    if (meta.getRiskLevel() > 2) return false;

                    // 2) 레버리지 / 인버스 제거
                    String name = meta.getName().toLowerCase();
                    if (name.contains("레버리지") || name.contains("인버스")) {
                        return false;
                    }

                    return true;
                })
                .toList();

        if (filtered.isEmpty()) return List.of();

        // 4️⃣ 점수 정규화 기준
        double maxValue = filtered.stream()
                .mapToDouble(r -> ((Number) r[1]).doubleValue())
                .max()
                .orElse(1.0);

        // 5️⃣ 결과 변환
        return filtered.stream()
                .limit(10)
                .map(r -> {
                    String isin = (String) r[0];
                    double raw = ((Number) r[1]).doubleValue();
                    double score = Math.round((raw / maxValue) * 100);

                    EtfMeta meta = metaMap.get(isin);

                    return new RecommendResultDto(
                            isin,
                            meta.getName(),
                            score,
                            "테마 [" + meta.getTheme() + "] 안전 인기 ETF",
                            meta.getTheme(),
                            "GREEN"
                    );
                })
                .toList();
    }
}
