package com.example.etfsj.service;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.domain.EtfPrice;
import com.example.etfsj.dto.EtfListDto;
import com.example.etfsj.repository.EtfMetaRepository;
import com.example.etfsj.repository.EtfPriceRepository;
import com.example.etfsj.repository.EtfSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final EtfSearchRepository etfSearchRepository;
    private final EtfPriceRepository etfPriceRepository; // ✅ 추가
    private final EtfMetaRepository etfMetaRepository;

    // ✅ 기존 검색 (유지)
    public List<EtfMeta> search(
            String keyword,
            String theme,
            Integer riskLevel,
            String expenseLevel
    ) {
        return etfSearchRepository.search(
                empty(keyword),
                empty(theme),
                riskLevel,
                empty(expenseLevel)
        );
    }

    // 🔥 페이징 검색 (신규)
    public Page<EtfMeta> search(
            String keyword,
            String theme,
            Integer riskLevel,
            String expenseLevel,
            Pageable pageable
    ) {
        List<EtfMeta> all = etfSearchRepository.search(
                empty(keyword),
                empty(theme),
                riskLevel,
                empty(expenseLevel)
        );

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());

        List<EtfMeta> content = all.subList(start, end);

        return new PageImpl<>(content, pageable, all.size());
    }

    private String empty(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }

    public Page<EtfListDto> searchList(
            String keyword,
            String theme,
            Integer riskLevel,
            String expenseLevel,
            Pageable pageable
    ) {
        // 1️⃣ 기본 목록 조회 (가격 포함)
        List<EtfListDto> rawList =
                etfSearchRepository.searchWithPrice(
                        empty(keyword),
                        empty(theme),
                        riskLevel,
                        empty(expenseLevel)
                );

        // 2️⃣ 수익률 계산해서 DTO 재생성
        List<EtfListDto> all = rawList.stream()
                .map(e -> {

                    Double dayRt   = calcReturn(e.getIsinCd(), 1);   // 1일
                    Double monthRt = calcReturn(e.getIsinCd(), 22);  // 1개월

                    return new EtfListDto(
                            e.getIsinCd(),
                            e.getName(),
                            e.getTheme(),
                            e.getRiskLevel(),
                            e.getTrPrc(),
                            dayRt,      // 🔥 DB flucRt 버림
                            monthRt
                    );
                })
                .toList();

        // 3️⃣ 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());

        List<EtfListDto> content = all.subList(start, end);

        return new PageImpl<>(content, pageable, all.size());
    }

    private Double calcReturn(String isinCd, int daysAgo) {

        // daysAgo + 1 개 필요 (오늘 포함)
        List<EtfPrice> prices =
                etfPriceRepository.findRecentPrices(
                        isinCd,
                        PageRequest.of(0, daysAgo + 1)
                );

        if (prices.size() <= daysAgo) return null;

        Double latest = prices.get(0).getClpr();
        Double past   = prices.get(daysAgo).getClpr();

        if (latest == null || past == null || past == 0) return null;

        return ((latest - past) / past) * 100;
    }

    public List<EtfPrice> getBannerEtfs() {
        return etfPriceRepository.findBannerEtfs(
                List.of(
                        "KODEX 미국반도체",
                        "KODEX 미국S&P500",
                        "TIGER 미국S&P500"
                )
        );
    }

    public List<EtfListDto> getRecommendByTheme(String theme, int limit) {

        List<EtfListDto> list =
                etfSearchRepository.searchWithPrice(
                        null,
                        theme,
                        null,
                        null
                );

        return list.stream()
                .limit(limit)
                .map(e -> new EtfListDto(
                        e.getIsinCd(),
                        e.getName(),
                        e.getTheme(),
                        e.getRiskLevel(),
                        e.getTrPrc(),
                        calcReturn(e.getIsinCd(), 1),
                        calcReturn(e.getIsinCd(), 22)
                ))
                .toList();
    }

    public List<EtfListDto> getRecommendSafe(int limit) {

        List<EtfMeta> metas =
                etfMetaRepository.findTop10ByRiskLevelLessThanEqualOrderByRiskLevelAsc(1);

        return metas.stream()
                .limit(limit)
                .map(m -> {
                    Double dayRt = calcReturn(m.getIsinCd(), 1);
                    return new EtfListDto(
                            m.getIsinCd(),
                            m.getName(),
                            m.getTheme(),
                            m.getRiskLevel(),
                            null,
                            dayRt,
                            null
                    );
                })
                .toList();
    }
}
