package com.example.etfsj.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RecommendResultDto {

    private String isinCd;
    private String etfName;
    private double score;
    private String reason;
    private String theme;
    private String signal; // GREEN/YELLOW/RED 표현(규민)
}
