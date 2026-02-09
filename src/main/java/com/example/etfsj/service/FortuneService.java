package com.example.etfsj.service;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.dto.FortuneResponseDto;
import com.example.etfsj.repository.EtfSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneService {

    private final EtfSearchRepository etfSearchRepository;

    // 운세 키워드 매핑 (LinkedHashMap: 위쪽에 있을수록 우선순위가 높습니다)
    private static final Map<String, String> KEYWORD_THEME_MAP = new LinkedHashMap<>();

    static {
        // ==========================================
        // 0. [긴급/강렬] 문장에 이 단어가 있으면 무조건 이걸로 매칭
        // ==========================================
        KEYWORD_THEME_MAP.put("대박", "레버리지");
        KEYWORD_THEME_MAP.put("천운", "레버리지");
        KEYWORD_THEME_MAP.put("횡재", "레버리지");
        KEYWORD_THEME_MAP.put("요행", "게임");      // 요행을 바라면 게임/엔터
        KEYWORD_THEME_MAP.put("모험", "신흥국");    // 모험은 신흥국/베트남/인도
        KEYWORD_THEME_MAP.put("재난", "골드");      // 재난 대비는 금
        KEYWORD_THEME_MAP.put("위기", "인버스");    // 위기엔 인버스
        KEYWORD_THEME_MAP.put("위험", "경기방어");  // 위험할 땐 방어주
        KEYWORD_THEME_MAP.put("도박", "증권");
        KEYWORD_THEME_MAP.put("최고", "우량주");

        // ==========================================
        // 1. [상승/성공] 긍정적 에너지 -> 성장/수익
        // ==========================================
        KEYWORD_THEME_MAP.put("성공", "IT");
        KEYWORD_THEME_MAP.put("상승", "증권");
        KEYWORD_THEME_MAP.put("정점", "반도체");
        KEYWORD_THEME_MAP.put("주인공", "미디어");
        KEYWORD_THEME_MAP.put("해결", "건설");
        KEYWORD_THEME_MAP.put("결실", "고배당");
        KEYWORD_THEME_MAP.put("행운", "엔터");
        KEYWORD_THEME_MAP.put("기적", "바이오");
        KEYWORD_THEME_MAP.put("도약", "2차전지");
        KEYWORD_THEME_MAP.put("발전", "로봇");
        KEYWORD_THEME_MAP.put("성취", "반도체");
        KEYWORD_THEME_MAP.put("승리", "게임");
        KEYWORD_THEME_MAP.put("보람", "ESG");
        KEYWORD_THEME_MAP.put("인정", "우량주");
        KEYWORD_THEME_MAP.put("칭찬", "화장품");

        // ==========================================
        // 2. [재물/경제] 돈/자산 -> 금융/고배당
        // ==========================================
        KEYWORD_THEME_MAP.put("재물", "금융");
        KEYWORD_THEME_MAP.put("금전", "은행");
        KEYWORD_THEME_MAP.put("목돈", "고배당");
        KEYWORD_THEME_MAP.put("지갑", "소비재");
        KEYWORD_THEME_MAP.put("투자", "증권");
        KEYWORD_THEME_MAP.put("수익", "증권");
        KEYWORD_THEME_MAP.put("이익", "은행");
        KEYWORD_THEME_MAP.put("자산", "리츠");
        KEYWORD_THEME_MAP.put("부동산", "리츠");
        KEYWORD_THEME_MAP.put("매매", "건설");
        KEYWORD_THEME_MAP.put("보너스", "유통");
        KEYWORD_THEME_MAP.put("복권", "레버리지");
        KEYWORD_THEME_MAP.put("적금", "은행");
        KEYWORD_THEME_MAP.put("통장", "금융");
        KEYWORD_THEME_MAP.put("현금", "단기자금");
        KEYWORD_THEME_MAP.put("부자", "럭셔리"); // 또는 우량주
        KEYWORD_THEME_MAP.put("쇼핑", "유통");
        KEYWORD_THEME_MAP.put("구매", "소비재");

        // ==========================================
        // 3. [안정/조심] 방어적 태도 -> 채권/안전자산 (여기가 중요!)
        // ==========================================
        KEYWORD_THEME_MAP.put("안정", "채권");      // "안정을 도모하라"
        KEYWORD_THEME_MAP.put("편안", "경기방어");
        KEYWORD_THEME_MAP.put("무난", "시장지수");
        KEYWORD_THEME_MAP.put("평범", "시장지수");
        KEYWORD_THEME_MAP.put("조심", "국채");
        KEYWORD_THEME_MAP.put("주의", "단기자금");
        KEYWORD_THEME_MAP.put("신중", "우량주");
        KEYWORD_THEME_MAP.put("구설", "경기방어");
        KEYWORD_THEME_MAP.put("망신", "보안");
        KEYWORD_THEME_MAP.put("사기", "보안");
        KEYWORD_THEME_MAP.put("다툼", "골드");
        KEYWORD_THEME_MAP.put("시비", "원자재");
        KEYWORD_THEME_MAP.put("충돌", "원자재");
        KEYWORD_THEME_MAP.put("손해", "인버스");
        KEYWORD_THEME_MAP.put("인내", "고배당");
        KEYWORD_THEME_MAP.put("기다림", "리츠");
        KEYWORD_THEME_MAP.put("관망", "달러");      // 달러선물
        KEYWORD_THEME_MAP.put("함정", "보안");
        KEYWORD_THEME_MAP.put("실수", "채권");
        KEYWORD_THEME_MAP.put("문제", "보험");      // "뜻밖의 문제"
        KEYWORD_THEME_MAP.put("걱정", "헬스케어");
        KEYWORD_THEME_MAP.put("고민", "의료");
        KEYWORD_THEME_MAP.put("방해", "경기방어");

        // ==========================================
        // 4. [이동/변화] 역동성 -> 운송/글로벌/기술
        // ==========================================
        KEYWORD_THEME_MAP.put("이동", "운송");
        KEYWORD_THEME_MAP.put("이사", "건설");
        KEYWORD_THEME_MAP.put("여행", "여행");
        KEYWORD_THEME_MAP.put("출장", "항공");
        KEYWORD_THEME_MAP.put("해외", "미국");
        KEYWORD_THEME_MAP.put("외국", "중국");
        KEYWORD_THEME_MAP.put("세계", "글로벌");
        KEYWORD_THEME_MAP.put("변동", "자동차");
        KEYWORD_THEME_MAP.put("운전", "자동차");
        KEYWORD_THEME_MAP.put("변화", "2차전지");
        KEYWORD_THEME_MAP.put("혁신", "AI");
        KEYWORD_THEME_MAP.put("새로운", "로봇");
        KEYWORD_THEME_MAP.put("시작", "벤처");
        KEYWORD_THEME_MAP.put("출발", "조선");
        KEYWORD_THEME_MAP.put("도전", "우주");
        KEYWORD_THEME_MAP.put("조류", "친환경");    // "세상 조류" -> 트렌드/환경
        KEYWORD_THEME_MAP.put("흐름", "에너지");

        // ==========================================
        // 5. [건강/상태] 신체 -> 헬스케어/바이오
        // ==========================================
        KEYWORD_THEME_MAP.put("건강", "바이오");
        KEYWORD_THEME_MAP.put("병원", "제약");
        KEYWORD_THEME_MAP.put("체력", "헬스케어");
        KEYWORD_THEME_MAP.put("운동", "헬스케어");
        KEYWORD_THEME_MAP.put("스트레스", "경기방어");
        KEYWORD_THEME_MAP.put("휴식", "여행");
        KEYWORD_THEME_MAP.put("피로", "커피");      // 식음료
        KEYWORD_THEME_MAP.put("불안", "보험");
        KEYWORD_THEME_MAP.put("우울", "엔터");
        KEYWORD_THEME_MAP.put("치유", "바이오");
        KEYWORD_THEME_MAP.put("회복", "의료기기");
        KEYWORD_THEME_MAP.put("컨디션", "제약");

        // ==========================================
        // 6. [인간관계/감정] -> 소비재/플랫폼
        // ==========================================
        KEYWORD_THEME_MAP.put("귀인", "화장품");
        KEYWORD_THEME_MAP.put("만남", "음식료");
        KEYWORD_THEME_MAP.put("약속", "유통");
        KEYWORD_THEME_MAP.put("친구", "게임");
        KEYWORD_THEME_MAP.put("연인", "미디어");
        KEYWORD_THEME_MAP.put("사랑", "엔터");
        KEYWORD_THEME_MAP.put("인기", "미디어");
        KEYWORD_THEME_MAP.put("소개", "플랫폼");
        KEYWORD_THEME_MAP.put("오해", "통신");
        KEYWORD_THEME_MAP.put("가족", "음식료");
        KEYWORD_THEME_MAP.put("결혼", "가구");
        KEYWORD_THEME_MAP.put("마음", "심리");      // 헬스케어/상담
        KEYWORD_THEME_MAP.put("감정", "미디어");

        // ==========================================
        // 7. [지적활동/업무] -> IT/반도체
        // ==========================================
        KEYWORD_THEME_MAP.put("합격", "반도체");
        KEYWORD_THEME_MAP.put("시험", "AI");
        KEYWORD_THEME_MAP.put("공부", "IT");
        KEYWORD_THEME_MAP.put("지식", "소프트웨어");
        KEYWORD_THEME_MAP.put("문서", "소프트웨어");
        KEYWORD_THEME_MAP.put("계약", "건설");
        KEYWORD_THEME_MAP.put("승진", "자동차");
        KEYWORD_THEME_MAP.put("책임", "ESG");
        KEYWORD_THEME_MAP.put("리더", "우량주");
        KEYWORD_THEME_MAP.put("면접", "의류");
        KEYWORD_THEME_MAP.put("발표", "미디어");
        KEYWORD_THEME_MAP.put("아이디어", "벤처");
        KEYWORD_THEME_MAP.put("계획", "건설");
        KEYWORD_THEME_MAP.put("생각", "AI");
        KEYWORD_THEME_MAP.put("발견", "바이오");

        // ==========================================
        // 8. [일상/자연] -> 생활밀착
        // ==========================================
        KEYWORD_THEME_MAP.put("정리", "친환경");
        KEYWORD_THEME_MAP.put("청소", "폐기물");    // 환경
        KEYWORD_THEME_MAP.put("선물", "유통");
        KEYWORD_THEME_MAP.put("식사", "농업");
        KEYWORD_THEME_MAP.put("맛", "음식료");
        KEYWORD_THEME_MAP.put("날씨", "에너지");
        KEYWORD_THEME_MAP.put("비", "스마트팜");
        KEYWORD_THEME_MAP.put("바람", "풍력");
        KEYWORD_THEME_MAP.put("해", "태양광");
        KEYWORD_THEME_MAP.put("컴퓨터", "반도체");
        KEYWORD_THEME_MAP.put("폰", "IT");
        KEYWORD_THEME_MAP.put("인터넷", "플랫폼");
        KEYWORD_THEME_MAP.put("게임", "메타버스");
        KEYWORD_THEME_MAP.put("꿈", "메타버스");
        KEYWORD_THEME_MAP.put("미래", "우주");
        KEYWORD_THEME_MAP.put("자리", "건설");      // "어울리지 않는 자리"
        KEYWORD_THEME_MAP.put("여건", "ESG");       // "좋은 여건"
    }

    public FortuneResponseDto getFortuneAndRecommend(String zodiacSign) {
        String fortuneText = crawlNaverFortune(zodiacSign);

        // 기본값
        String matchedKeyword = "평온";
        String targetTheme = "시장지수";

        boolean isMatched = false;

        // 맵 순회 (우선순위 순서대로)
        for (Map.Entry<String, String> entry : KEYWORD_THEME_MAP.entrySet()) {
            if (fortuneText.contains(entry.getKey())) {
                matchedKeyword = entry.getKey();
                targetTheme = entry.getValue();
                isMatched = true;
                // 가장 중요도 높은 키워드를 찾으면 바로 중단
                break;
            }
        }

        if (!isMatched) {
            log.info("매칭 실패 (기본값 사용). 운세 텍스트: {}", fortuneText);
        }

        // DB 검색
        List<EtfMeta> etfs = etfSearchRepository.search(targetTheme, null, null, null);

        // 검색 결과 0개일 때 대체 로직
        if (etfs.isEmpty()) {
            log.warn("테마 '{}' ETF 없음. 시장지수로 대체.", targetTheme);
            targetTheme = "시장지수";
            etfs = etfSearchRepository.search(targetTheme, null, null, null);
        }

        if (etfs.isEmpty()) {
            etfs = etfSearchRepository.findAll();
        }

        // 최대 3개 랜덤 섞어서 반환
        if (etfs.size() > 3) {
            Collections.shuffle(etfs);
            etfs = etfs.subList(0, 3);
        }

        return new FortuneResponseDto(zodiacSign, fortuneText, matchedKeyword, targetTheme, etfs);
    }

    private String crawlNaverFortune(String zodiacSign) {
        String url = "https://search.naver.com/search.naver?query=" + zodiacSign + " 운세";
        try {
            Document doc = Jsoup.connect(url).get();
            Element contentElement = doc.selectFirst(".text._cs_fortune_text");

            if (contentElement != null) {
                return contentElement.text();
            } else {
                return zodiacSign + "님, 오늘은 차분한 마음으로 내실을 다지기 좋은 날입니다.";
            }
        } catch (IOException e) {
            log.error("운세 크롤링 실패", e);
            return "우주의 기운을 잠시 불러오지 못했습니다.";
        }
    }
}