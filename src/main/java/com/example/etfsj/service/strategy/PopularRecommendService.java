package com.example.etfsj.service.strategy;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.domain.User;
import com.example.etfsj.dto.RecommendResultDto;
import com.example.etfsj.repository.EtfMetaRepository;
import com.example.etfsj.repository.EtfPriceRepository;
import com.example.etfsj.util.PeriodUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PopularRecommendService {

    private final EtfMetaRepository etfMetaRepository;
    private final EtfPriceRepository etfPriceRepository;

    public List<RecommendResultDto> recommend(User user) {

        String latestBasDt = etfPriceRepository.findGlobalLatestBasDt();
        if (latestBasDt == null) return List.of();

        String fromDt = PeriodUtil.calcFromDt(latestBasDt, "6M");

        List<Object[]> rows = etfPriceRepository.findAvgTradeAmount(fromDt);
        if (rows == null || rows.isEmpty()) return List.of();

        List<Double> logScores = new ArrayList<>();
        for (Object[] row : rows) {
            if (row[1] == null) continue;
            double v = ((Number) row[1]).doubleValue();
            if (v > 0) logScores.add(Math.log10(v));
        }
        if (logScores.isEmpty()) return List.of();

        double min = Collections.min(logScores);
        double max = Collections.max(logScores);
        double denom = max - min;

        List<RecommendResultDto> result = new ArrayList<>();

        for (Object[] row : rows) {
            String isin = (String) row[0];
            double avg = ((Number) row[1]).doubleValue();
            if (avg <= 0) continue;

            EtfMeta meta = etfMetaRepository.findById(isin).orElse(null);
            if (meta == null) continue;

            double score = denom <= 0
                    ? 50.0
                    : (Math.log10(avg) - min) / denom * 100.0;

            score = Math.round(score * 100.0) / 100.0;

            result.add(new RecommendResultDto(
                    isin,
                    meta.getName(),
                    score,
                    "거래대금 기반 인기 ETF",
                    meta.getTheme(),
                    meta.getRiskLevel() <= 2 ? "GREEN" :
                            meta.getRiskLevel() <= 4 ? "YELLOW" : "RED"
            ));
        }

        return result.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(10)
                .toList();
    }
}
