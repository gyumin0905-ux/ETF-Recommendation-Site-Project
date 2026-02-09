package com.example.etfsj.service;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.dto.CompareItemDto;
import com.example.etfsj.dto.CompareResultDto;
import com.example.etfsj.dto.CompareReturnChartDto;
import com.example.etfsj.repository.EtfMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CompareService {

    private final EtfMetaRepository etfMetaRepository;
    private final EtfChartService etfChartService;

    /** ✅ 서로 다른 테마 ETF 비교 */
    public CompareResultDto compareRandomTheme(String mode) {

        // 1️⃣ 전체 테마 조회
        List<String> themes = new ArrayList<>(etfMetaRepository.findDistinctThemes());

        if (themes.size() < 2) {
            throw new IllegalStateException("비교 가능한 테마가 2개 이상 필요합니다.");
        }

        // 2️⃣ 테마 2개 랜덤 선택 (서로 다름)
        Collections.shuffle(themes);
        String leftTheme = themes.get(0);
        String rightTheme = themes.get(1);

        // 3️⃣ 각 테마에서 ETF 1개씩 선택
        EtfMeta left = pickEtf(leftTheme, mode);
        EtfMeta right = pickEtf(rightTheme, mode);

        return new CompareResultDto(
                leftTheme + " vs " + rightTheme,
                mode,
                toDto(left),
                toDto(right)
        );
    }

    /** ✅ 추가: 랜덤 비교 TOP5 */
    public List<CompareResultDto> compareRandomTop5(String mode) {
        List<CompareResultDto> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(compareRandomTheme(mode));
        }
        return list;
    }

    /** 테마 내 ETF 선택 */
    private EtfMeta pickEtf(String theme, String mode) {
        List<EtfMeta> list =
                new ArrayList<>(etfMetaRepository.findByTheme(theme));

        if (list.isEmpty()) {
            throw new IllegalStateException(theme + " 테마에 ETF가 없습니다.");
        }

        if ("POPULAR".equalsIgnoreCase(mode)) {
            list.sort(Comparator.comparing(EtfMeta::getName));
            return list.get(0);
        } else {
            Collections.shuffle(list);
            return list.get(0);
        }
    }

    private CompareItemDto toDto(EtfMeta m) {
        return new CompareItemDto(
                m.getName(),
                m.getTheme(),
                m.getRiskLevel(),
                m.getIsinCd(),
                buildStrengths(m)
        );
    }

    /** 장점 자동 생성 */
    private List<String> buildStrengths(EtfMeta m) {
        List<String> list = new ArrayList<>();

        if (m.getRiskLevel() <= 2) {
            list.add("위험도가 낮아 안정적인 투자에 적합");
        } else {
            list.add("변동성이 있어 공격적인 투자자에게 적합");
        }

        if (m.getTheme().contains("국내")) {
            list.add("국내 시장 중심의 안정적 구성");
        } else if (m.getTheme().contains("해외")) {
            list.add("해외 자산 분산 투자 효과");
        } else if (m.getTheme().contains("MMF")) {
            list.add("단기 자금 운용에 적합");
        }

        return list;
    }

    /** ✅ 수익률 비교 차트 (핵심 수정 포인트) */
    public CompareReturnChartDto getCompareReturnChart(
            String isin1,
            String isin2,
            String period
    ) {
        return new CompareReturnChartDto(
                etfChartService.getDateLabels(isin1, period),
                new CompareReturnChartDto.Line(
                        isin1,
                        etfChartService.getReturnSeries(isin1, period)
                ),
                new CompareReturnChartDto.Line(
                        isin2,
                        etfChartService.getReturnSeries(isin2, period)
                )
        );
    }
}