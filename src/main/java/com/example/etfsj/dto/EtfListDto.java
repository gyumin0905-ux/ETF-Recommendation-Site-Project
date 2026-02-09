package com.example.etfsj.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EtfListDto {

    private String isinCd;
    private String name;
    private String theme;
    private Integer riskLevel;

    private Long trPrc;     // 거래대금
    private Double flucRt;  // 등락률
    private Double monthRt;    // 🔥 1개월 수익률 (신규)
}
