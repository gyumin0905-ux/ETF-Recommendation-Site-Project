package com.example.etfsj.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EtfCompanyService {

    private static final List<String> ETF_COMPANIES = List.of(
            "KODEX",
            "TIGER",
            "HANARO",
            "ACE",
            "TIMEFOLIO",
            "FOCUS"
    );

    public List<String> getRandomCompanies(int count) {
        List<String> shuffled = new ArrayList<>(ETF_COMPANIES);
        Collections.shuffle(shuffled);
        return shuffled.stream()
                .limit(count)
                .toList();
    }
}
