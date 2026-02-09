package com.example.etfsj.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

//DB ETF_PRICE에서 월별로 집계한 결과를 담는 DTO

@Getter
@AllArgsConstructor
public class MonthlyPrice {

    private String month;   // YYYY-MM
    private double price;   // 해당 월 대표 가격 (ex: 평균 or 말일 종가)
}
