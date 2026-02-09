package com.example.etfsj.dto;

import com.example.etfsj.domain.EtfMeta;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class FortuneResponseDto {
    private String zodiacSign;      // 별자리 이름 (예: 물병자리)
    private String fortuneText;     // 크롤링해온 운세 내용 (예: "금전운이 폭발합니다...")
    private String keyword;         // 운세에서 뽑아낸 핵심 키워드 (예: "재물")
    private String matchedTheme;    // 키워드랑 연결된 ETF 테마 (예: "금융")
    private List<EtfMeta> recommendEtfs; // 추천된 ETF 리스트 (DB에서 조회한 결과)

    // 생성자
    public FortuneResponseDto(String zodiacSign, String fortuneText, String keyword, String matchedTheme, List<EtfMeta> recommendEtfs) {
        this.zodiacSign = zodiacSign;
        this.fortuneText = fortuneText;
        this.keyword = keyword;
        this.matchedTheme = matchedTheme;
        this.recommendEtfs = recommendEtfs;
    }
}