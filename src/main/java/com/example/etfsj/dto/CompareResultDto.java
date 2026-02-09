package com.example.etfsj.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompareResultDto {

    private String theme;
    private String mode;
    private CompareItemDto left;
    private CompareItemDto right;
}
