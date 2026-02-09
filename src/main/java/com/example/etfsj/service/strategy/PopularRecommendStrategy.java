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
public class PopularRecommendStrategy implements RecommendStrategy {

    private final EtfPriceRepository etfPriceRepository;
    private final EtfMetaRepository etfMetaRepository;

    @Override
    public String getKey() {
        return "POPULAR";
    }

    @Override
    public List<RecommendResultDto> recommend(User user) {

        // 🔥 최근 30일 기준
        String fromDt = PeriodUtil.daysAgo(30);

        // [ISIN, avgTradeAmount]
        List<Object[]> rows = etfPriceRepository.findAvgTradeAmount(fromDt);
        if (rows.isEmpty()) {
            return List.of();
        }

        // 🔥 최대 거래대금 (정규화 기준)
        double maxValue = rows.stream()
                .mapToDouble(r -> ((Number) r[1]).doubleValue())
                .max()
                .orElse(1.0);

        // 메타 정보 미리 로딩
        Map<String, EtfMeta> metaMap =
                etfMetaRepository.findAll().stream()
                        .collect(Collectors.toMap(
                                EtfMeta::getIsinCd,
                                m -> m
                        ));

        return rows.stream()
                .limit(10)
                .map(row -> {
                    String isin = (String) row[0];
                    double raw = ((Number) row[1]).doubleValue();

                    // ⭐ 0~100 정규화
                    double score = Math.round((raw / maxValue) * 100);

                    EtfMeta meta = metaMap.get(isin);
                    if (meta == null) return null;

                    return new RecommendResultDto(
                            isin,
                            meta.getName(),
                            score,
                            "최근 거래대금 기준 인기 ETF",
                            meta.getTheme(),
                            "ORANGE"
                    );
                })
                .filter(r -> r != null)
                .toList();
    }
}
