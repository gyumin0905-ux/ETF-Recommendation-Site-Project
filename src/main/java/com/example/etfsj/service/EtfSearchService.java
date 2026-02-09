package com.example.etfsj.service;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.repository.EtfMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EtfSearchService {

    private final EtfMetaRepository etfMetaRepository;

    // ===============================
    // 🔍 검색
    // ===============================
    public List<EtfMeta> search(String theme, Integer riskLevel, String expenseLevel) {
        if ("".equals(theme)) theme = null;
        if ("".equals(expenseLevel)) expenseLevel = null;

        return etfMetaRepository.searchEtfs(theme, riskLevel, expenseLevel);
    }

    // ===============================
    // 🔥 ETF 상세 (ISIN 기준)
    // ===============================
    public Optional<EtfMeta> findByIsin(String isin) {
        return etfMetaRepository.findById(isin); // ✅ PK 사용
    }


}
