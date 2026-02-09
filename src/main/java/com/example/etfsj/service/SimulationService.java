package com.example.etfsj.service;

import com.example.etfsj.domain.SimulationPoint;
import com.example.etfsj.domain.SimulationResult;
import com.example.etfsj.domain.User;
import com.example.etfsj.repository.EtfMonthlyPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final EtfMonthlyPriceRepository priceRepo;

    public SimulationResult simulate(
            String isin,
            int monthlyAmount,
            String startYm,
            String endYm,
            User user
    ) {
        List<Object[]> rows =
                priceRepo.findMonthlyPrices(isin, startYm, endYm);

        double totalInvested = 0.0;
        double totalShares = 0.0;

        List<SimulationPoint> series = new ArrayList<>();

        for (Object[] r : rows) {
            String month = (String) r[0];
            double price = ((Number) r[1]).doubleValue();

            // ✅ 이번 달 매수 수량
            double quantity = monthlyAmount / price;

            totalShares += quantity;
            totalInvested += monthlyAmount;

            // ✅ 누적 평균 단가
            double avgPrice = totalInvested / totalShares;

            // ✅ 누적 평가금액
            double value = totalShares * price;

            series.add(new SimulationPoint(
                    month,
                    quantity,
                    avgPrice,
                    value
            ));
        }

        double finalValue = series.isEmpty()
                ? 0
                : series.get(series.size() - 1).getValue();

        double roi = totalInvested == 0
                ? 0
                : (finalValue - totalInvested) / totalInvested;

        String summary = generateSummary(
                user != null ? user.getExperience() : "beginner",
                series.size(),
                monthlyAmount,
                totalInvested,
                finalValue,
                roi
        );

        return new SimulationResult(
                totalInvested,
                finalValue,
                roi,
                series,
                summary
        );
    }

    // ===============================
    // 🔥 투자 성향별 설명 생성
    // ===============================
    private String generateSummary(
            String experience,
            int months,
            int monthlyAmount,
            double totalInvested,
            double finalValue,
            double roi
    ) {
        if ("advanced".equalsIgnoreCase(experience)) {
            return String.format(
                    """
                    총 %d개월 동안 월 %,d원을 투자하여
                    총 투자금은 %,d원입니다.

                    최종 평가금액은 %,d원이며
                    누적 수익률은 %.2f%%입니다.

                    정기 적립식(DCA) 구조상
                    가격 변동 구간에서 평균 단가가 안정화되는 효과가 나타났습니다.
                    """,
                    months,
                    monthlyAmount,
                    (long) totalInvested,
                    (long) finalValue,
                    roi * 100
            );
        }

        // beginner / default
        return String.format(
                """
                매달 같은 금액을 %d개월 동안 꾸준히 투자했어요.

                총 %,d원을 넣었고
                마지막에는 약 %,d원이 되었어요.

                중간에 가격이 내려가도
                계속 투자하면 이런 결과가 나올 수 있어요 🙂
                """,
                months,
                (long) totalInvested,
                (long) finalValue
        );
    }
}
