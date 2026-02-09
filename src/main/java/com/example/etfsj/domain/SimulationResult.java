package com.example.etfsj.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

//최종 결과 DTO
//중간 수익률 ❌ / 최종 결과 ⭕” 철학 그대로

@Getter
@AllArgsConstructor
public class SimulationResult {

    private double totalInvested;
    private double finalValue;
    private double roi;
    private List<SimulationPoint> series;

    //  투자 성향별 자동 설명
    private String summary;
}
