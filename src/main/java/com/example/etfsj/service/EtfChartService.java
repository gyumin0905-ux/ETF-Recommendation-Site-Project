package com.example.etfsj.service;

import com.example.etfsj.domain.EtfPrice;
import com.example.etfsj.repository.EtfPriceRepository;
import com.example.etfsj.util.PeriodUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EtfChartService {

    private final EtfPriceRepository priceRepository;

    // ===============================
    // 🔥 가격 데이터
    // ===============================
    public List<EtfPrice> getPrices(String isin, String period) {

        String latest = priceRepository.findLatestBasDt(isin);
        if (latest == null) return List.of();

        // ✅ String → String (LocalDate 절대 안 씀)
        String fromDt = PeriodUtil.calcFromDt(latest, period);

        return priceRepository.findByPeriod(isin, fromDt);
    }

    // ===============================
    // 🔥 기간 수익률 (%)
    // ===============================
    public Double calcReturn(String isin, String period) {

        List<EtfPrice> prices = getPrices(isin, period);
        if (prices.size() < 2) return null;

        double start = prices.get(0).getClpr();
        double end = prices.get(prices.size() - 1).getClpr();

        if (start == 0) return null;

        return (end - start) / start * 100;
    }

    // ===============================
    // 🔥 변동성 (연환산)
    // ===============================
    public Double calcVolatility(String isin, String period) {

        List<EtfPrice> prices = getPrices(isin, period);
        if (prices.size() < 2) return null;

        List<Double> dailyReturns = new ArrayList<>();

        for (int i = 1; i < prices.size(); i++) {
            double prev = prices.get(i - 1).getClpr();
            double curr = prices.get(i).getClpr();
            if (prev == 0) continue;
            dailyReturns.add((curr - prev) / prev);
        }

        if (dailyReturns.size() < 2) return null;

        double avg = dailyReturns.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        double variance = dailyReturns.stream()
                .mapToDouble(r -> Math.pow(r - avg, 2))
                .average()
                .orElse(0);

        return Math.sqrt(variance) * Math.sqrt(252) * 100;
    }

    // ===============================
    // 🔧 [추가] ETF 최신 가격 1건 조회
    // ===============================
    public EtfPrice findLatestPrice(String isin) {
        return priceRepository
                .findRecentPrices(isin, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
    }

    // ===============================
    // 🔧 [추가] 등락률 계산 (VS 기반)
    // ===============================
    public Double calcFlucRate(EtfPrice price) {
        if (price == null || price.getVs() == null) return null;

        double prev = price.getClpr() - price.getVs();
        if (prev == 0) return null;

        return Math.round((price.getVs() / prev) * 10000.0) / 100.0;
    }

    // ===============================
    // 🔥 차트용 날짜 라벨
    // ===============================
    public List<String> getDateLabels(String isin, String period) {

        List<EtfPrice> prices = getPrices(isin, period);
        List<String> labels = new ArrayList<>();

        for (EtfPrice p : prices) {
            labels.add(p.getBasDt()); // YYYYMMDD 그대로
        }

        return labels;
    }

    // ===============================
    // 🔥 차트용 수익률 시계열 (%)
    // ===============================
    public List<Double> getReturnSeries(String isin, String period) {

        List<EtfPrice> prices = getPrices(isin, period);
        List<Double> series = new ArrayList<>();

        if (prices.isEmpty()) return series;

        double base = prices.get(0).getClpr();
        if (base == 0) return series;

        for (EtfPrice p : prices) {
            double r = (p.getClpr() - base) / base * 100;
            series.add(Math.round(r * 100.0) / 100.0); // 소수 2자리
        }

        return series;
    }
}
