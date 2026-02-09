package com.example.etfsj.service;

import com.example.etfsj.dto.EtfCompositionDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EtfCompositionService {

    // 🔒 원본은 불변으로 둬도 됨
    private static final List<String> ASSET_POOL = List.of(
            "TESLA INC", "NVIDIA", "APPLE", "MICROSOFT", "AMAZON",
            "META", "ALPHABET", "NETFLIX", "AMD", "INTEL",
            "현대차", "기아", "HL만도", "에스피지", "로보티즈",
            "삼성전자", "SK하이닉스", "LG에너지솔루션", "포스코홀딩스", "네이버",
            "카카오", "두산로보틱스", "한화에어로스페이스", "현대모비스", "LG전자",
            "ASML", "TSMC", "QUALCOMM", "BROADCOM", "SIEMENS"
    );

    public List<EtfCompositionDto> generateTop5Composition() {

        // ✅ 반드시 가변 리스트로 복사
        List<String> shuffled = new ArrayList<>(ASSET_POOL);
        Collections.shuffle(shuffled);

        List<String> selected = shuffled.subList(0, 5);

        int[][] ranges = {
                {30, 45}, // 1위
                {15, 25}, // 2위
                {10, 20}, // 3위
                {5, 15},  // 4위
                {5, 10}   // 5위
        };

        Random random = new Random();
        int[] weights = new int[5];

        for (int i = 0; i < ranges.length; i++) {
            weights[i] = random.nextInt(ranges[i][1] - ranges[i][0] + 1)
                    + ranges[i][0];
        }

        // 합계 보정
        int sum = Arrays.stream(weights).sum();
        weights[0] += (100 - sum);

        List<EtfCompositionDto> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            result.add(new EtfCompositionDto(selected.get(i), weights[i]));
        }

        result.sort((a, b) -> b.getWeight() - a.getWeight());
        return result;
    }
}
