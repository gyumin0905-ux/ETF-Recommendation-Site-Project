package com.example.etfsj.service;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.domain.EtfPrice;
import com.example.etfsj.dto.EtfComparisonResultDto;
import com.example.etfsj.dto.EtfSingleCompareDto;
import com.example.etfsj.repository.EtfMetaRepository;
import com.example.etfsj.repository.EtfPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompareEtfService {

    private final EtfMetaRepository metaRepo;
    private final EtfPriceRepository priceRepo;

    public EtfComparisonResultDto compare(String leftIsin, String rightIsin) {
        return new EtfComparisonResultDto(
                buildSingle(leftIsin),
                buildSingle(rightIsin)
        );
    }

    private EtfSingleCompareDto buildSingle(String isin) {

        var meta = metaRepo.findByIsinCd(isin).orElseThrow();
        var prices = priceRepo.findRecentPrices(isin, PageRequest.of(0, 365));

        return new EtfSingleCompareDto(
                isin,
                meta.getName(),
                calcReturn(prices, 30),
                calcReturn(prices, 90),
                calcReturn(prices, 365),
                calcVolatility(prices),      // 연환산 변동성 %
                calcMdd(prices),
                toEok(calcAvgVolume(prices, 30)), // 억 단위 변환
                calcRiskScore(meta, prices)
        );
    }

    private double calcReturn(List<EtfPrice> prices, int period) {
        if (prices.size() < period) return 0.0;
        double start = prices.get(period - 1).getClpr();
        double end = prices.get(0).getClpr();
        return (end - start) / start * 100;
    }

    private double calcVolatility(List<EtfPrice> prices) {
        if (prices.size() < 2) return 0.0;

        var closes = prices.stream()
                .map(EtfPrice::getClpr)
                .filter(v -> v != null && v > 0)
                .toList();

        if (closes.size() < 2) return 0.0;

        // 수익률 계산
        var returns = new double[closes.size() - 1];
        for (int i = 1; i < closes.size(); i++) {
            returns[i - 1] = (closes.get(i) - closes.get(i - 1)) / closes.get(i - 1);
        }

        // 표준편차 계산
        double mean = Arrays.stream(returns).average().orElse(0);
        double variance = Arrays.stream(returns).map(r -> Math.pow(r - mean, 2)).sum() / returns.length;
        double dailyVol = Math.sqrt(variance);

        // 연환산 변환 + %
        return dailyVol * Math.sqrt(252) * 100;
    }

    private double calcMdd(List<EtfPrice> prices) {
        double peak = Double.MIN_VALUE;
        double maxDd = 0;

        for (EtfPrice p : prices) {
            peak = Math.max(peak, p.getClpr());
            double dd = (peak - p.getClpr()) / peak;
            maxDd = Math.max(maxDd, dd);
        }

        return maxDd * 100;
    }

    private long calcAvgVolume(List<EtfPrice> prices, int days) {
        return (long) prices.stream()
                .limit(days)
                .map(EtfPrice::getTrPrc)
                .filter(v -> v != null && v > 0)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }

    private double calcRiskScore(EtfMeta meta, List<EtfPrice> prices) {
        return meta.getRiskLevel() != null ? meta.getRiskLevel() : 3;
    }

    private double toEok(long v) {
        return v / 100_000_000.0;
    }
}
