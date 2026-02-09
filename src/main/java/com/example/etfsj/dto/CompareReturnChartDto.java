package com.example.etfsj.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CompareReturnChartDto {

    private List<String> labels;
    private Line left;
    private Line right;

    @Getter
    @AllArgsConstructor
    public static class Line {
        private String name;
        private List<Double> data;
    }
}