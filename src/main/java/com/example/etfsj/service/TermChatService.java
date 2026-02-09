package com.example.etfsj.service;

import com.example.etfsj.domain.Term;
import com.example.etfsj.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TermChatService {

    private final TermRepository termRepository;

    // ✅ 엑셀 기준 별칭(줄임말/약어) 사전
    private static final Map<String, String> ALIAS_MAP = Map.ofEntries(
            Map.entry("mdd", "최대낙폭(MDD)"),
            Map.entry("낙폭", "최대낙폭(MDD)"),
            Map.entry("최대하락", "최대낙폭(MDD)"),

            Map.entry("추적에러", "추적오차"),
            Map.entry("에러", "추적오차"),

            Map.entry("원자재", "원자재 ETF"),
            Map.entry("주식형", "주식형 ETF"),
            Map.entry("채권형", "채권형 ETF"),

            Map.entry("레버리지", "레버리지 ETF"),
            Map.entry("인버스", "인버스 ETF"),

            Map.entry("배당", "분배금"),
            Map.entry("총비용", "총보수"),
            Map.entry("운영보수", "운용보수")
    );

    public List<String> getAllTermNames() {
        return termRepository.findAll().stream()
                .map(Term::getName)
                .toList();
    }

    public String answer(String question) {
        if (question == null || question.isBlank()) {
            return "질문을 입력해줘 🙂";
        }

        Term term = findTermFromQuestion(question);

        if (term == null) {
            return """
                   음… 그 용어는 아직 제 사전에 없어요 😅
                   위에 있는 버튼 중 하나를 눌러보거나,
                   용어를 정확히 입력해줘!
                   """.trim();
        }

        return buildAiStyleAnswer(term);
    }

    // =========================
    // 🔍 용어 인식 핵심 로직
    // =========================
    private Term findTermFromQuestion(String question) {

        // 1️⃣ 질문 정규화
        String normalized = normalize(question);

        // 2️⃣ alias 치환 (여기서는 변경 가능)
        for (var entry : ALIAS_MAP.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                normalized = entry.getValue();
                break;
            }
        }

        // 🔒 람다에서 사용할 최종 문자열 (실질적 final)
        final String q = normalize(normalized);

        // 3️⃣ 정확 일치
        var exact = termRepository.findByName(q);
        if (exact.isPresent()) return exact.get();

        // 4️⃣ 포함 매칭 (긴 용어 우선)
        return termRepository.findAll().stream()
                .sorted((a, b) -> b.getName().length() - a.getName().length())
                .filter(t -> q.contains(normalize(t.getName())))
                .findFirst()
                .orElse(null);
    }

    // =========================
    // 🧹 질문 정규화
    // =========================
    private String normalize(String q) {
        return q.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("(이|가|은|는|을|를|의)", "")
                .replaceAll("(뭐야|뭐임|뭔데|알려줘|설명|뜻)", "");
    }

    // =========================
    // 💬 답변 생성
    // =========================
    private String buildAiStyleAnswer(Term term) {

        String name = term.getName();
        String baseDesc = term.getDescription();

        String detail = term.getDetail();
        String friendly = (detail == null || detail.isBlank())
                ? makeFriendly(baseDesc, name)
                : detail;

        return """
           🤖 %s 설명해줄게요!

           ✅ 한 줄 요약
           - %s

           ✅ 쉽게 풀어서
           - %s

           ✅ 투자할 때 이렇게 보면 좋아요
           - “%s”를 볼 때는, **내가 감당 가능한 위험/수수료/변동성**과 연결해서 보면 좋아요 🙂

           더 궁금하면 “%s 예시 들어줘”처럼 물어봐도 돼요!
           """.formatted(
                name,
                summarize(baseDesc, name),
                friendly,
                name,
                name
        ).trim();
    }

    private String summarize(String desc, String name) {
        if (desc == null || desc.isBlank()) {
            return name + "는 ETF 투자에서 자주 나오는 중요한 개념이에요.";
        }

        String s = desc.replace("\n", " ").trim();
        if (s.length() > 60) {
            s = s.substring(0, 60) + "...";
        }
        return s;
    }

    private String makeFriendly(String desc, String name) {
        if (desc == null || desc.isBlank()) {
            return name + "는 쉽게 말해 ‘ETF에서 자주 확인하는 핵심 기준’ 정도로 생각하면 돼요.";
        }

        return "쉽게 말하면, " + desc.trim() + " 라고 보면 돼요.";
    }
}
