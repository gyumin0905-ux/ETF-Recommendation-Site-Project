package com.example.etfsj.dto;

public record EtfSingleCompareDto(
        String isin,
        String name,
        double return1m,
        double return3m,
        double return1y,
        double volatility,
        double mdd,
        double volumeAvg,
        double riskScore
) {}