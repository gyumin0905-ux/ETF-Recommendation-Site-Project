package com.example.etfsj.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CompareItemDto {

    private String name;
    private String theme;
    private Integer riskLevel;
    private String isinCd;
    private List<String> strengths;
}
