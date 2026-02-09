package com.example.etfsj.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompareEtfDto {

    private String isinCd;
    private String name;
    private String theme;
    private int riskLevel;
    private double avgTradeAmount;
}
