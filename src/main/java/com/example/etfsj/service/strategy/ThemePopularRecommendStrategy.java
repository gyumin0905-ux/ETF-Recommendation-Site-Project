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
public class ThemePopularRecommendStrategy implements RecommendStrategy {

    private final EtfPriceRepository etfPriceRepository;
    private final EtfMetaRepository etfMetaRepository;

    /** ⭐ URL type=THEME_POPULAR 과 반드시 일치 */
    @Override
    public String getKey() {
        return "THEME_POPULAR";
    }

    /** theme 없으면 전체 테마 인기 */
    @Override
    public List<RecommendResultDto> recommend(User user) {
        return recommendByTheme(user, null);
    }

    /** ⭐ 핵심: 테마 + 인기 */
    public List<RecommendResultDto> recommendByTheme(User user, String theme) {

        String fromDt = PeriodUtil.daysAgo(30);

        // 1️⃣ 거래대금 기반 인기 데이터
        List<Object[]> rows = etfPriceRepository.findAvgTradeAmount(fromDt);
        if (rows.isEmpty()) return List.of();

        // 2️⃣ 메타 정보 로딩 (ISIN → EtfMeta)
        Map<String, EtfMeta> metaMap =
                etfMetaRepository.findAll().stream()
                        .filter(m -> m.getTheme() != null)
                        .collect(Collectors.toMap(
                                EtfMeta::getIsinCd,
                                m -> m
                        ));

        // 3️⃣ ⭐ 테마 필터 (부분 포함!)
        List<Object[]> filtered = rows.stream()
                .filter(r -> {
                    EtfMeta meta = metaMap.get((String) r[0]);
                    if (meta == null) return false;

                    // theme 없으면 전체
                    if (theme == null || theme.isBlank()) return true;

                    // ⭐ 핵심 수정: equals → contains
                    return meta.getTheme().contains(theme);
                })
                .toList();

        if (filtered.isEmpty()) return List.of();

        // 4️⃣ 점수 정규화 기준
        double maxValue = filtered.stream()
                .mapToDouble(r -> ((Number) r[1]).doubleValue())
                .max()
                .orElse(1.0);

        // 5️⃣ DTO 변환 (0~100 스케일)
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
                            "테마 [" + meta.getTheme() + "] 인기 ETF",
                            meta.getTheme(),
                            "PURPLE"
                    );
                })
                .toList();
    }
}
