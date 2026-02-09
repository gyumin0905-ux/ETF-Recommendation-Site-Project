package com.example.etfsj.dto;

public record EtfComparisonResultDto(
        EtfSingleCompareDto left,
        EtfSingleCompareDto right
    ) {
}
