package com.example.etfsj.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PeriodUtil {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * toDt(yyyyMMdd) 기준으로
     * period (1M,3M,6M,1Y) 이전 날짜를 yyyyMMdd String으로 반환
     */
    public static String calcFromDt(String toDt, String period) {

        LocalDate base = LocalDate.parse(toDt, FMT);

        return switch (period) {
            case "1M" -> base.minusMonths(1).format(FMT);
            case "3M" -> base.minusMonths(3).format(FMT);
            case "6M" -> base.minusMonths(6).format(FMT);
            case "1Y" -> base.minusYears(1).format(FMT);
            default -> base.minusMonths(6).format(FMT);
        };
    }

    /**
     * 오늘 기준 n일 전 날짜를 yyyyMMdd String으로 반환
     * ex) daysAgo(30) -> 20250114
     */
    public static String daysAgo(int days) {
        return LocalDate.now()
                .minusDays(days)
                .format(FMT);
    }

    /**
     * 오늘 날짜 yyyyMMdd
     */
    public static String today() {
        return LocalDate.now().format(FMT);
    }
}
