package com.example.etfsj.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

//(월별 시뮬레이션 결과 DTO)
//그래프용 + 흐름 설명용

@Getter
@AllArgsConstructor
public class SimulationPoint {

    private String month;      // YYYYMM
    private double shares;     // 누적 보유 수량
    private double totalShares;  // 누적 보유 수량
    private double value;      // 해당 월 평가금액
}

