package com.example.etfsj.config;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.repository.EtfMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@RequiredArgsConstructor
public class EtfMetaCsvLoader implements CommandLineRunner {

    private final EtfMetaRepository etfMetaRepository;

    @Override
    public void run(String... args) throws Exception {

        // 🔒 이미 데이터 있으면 스킵
        if (etfMetaRepository.count() > 0) {
            System.out.println("ETF_META already loaded. skip.");
            return;
        }

        ClassPathResource resource =
                new ClassPathResource("data/kr_etf_processed_REBUILT_FROM_PRICE.csv");

        int saved = 0;
        int skipped = 0;

        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(resource.getInputStream()))) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {

                // 1️⃣ 헤더 스킵
                if (first) {
                    first = false;
                    continue;
                }

                // 2️⃣ 빈 줄 방어
                if (line.trim().isEmpty()) {
                    skipped++;
                    continue;
                }

                String[] t = line.split(",");

                // 3️⃣ 컬럼 개수 방어 (최소 6개)
                if (t.length < 6) {
                    skipped++;
                    continue;
                }

                String etfId = t[0].trim();

                // 4️⃣ PK / ISIN null 방어
                if (etfId.isEmpty()) {
                    skipped++;
                    continue;
                }

                // 5️⃣ 중복 방지
                if (etfMetaRepository.existsById(etfId)) {
                    skipped++;
                    continue;
                }

                EtfMeta meta = new EtfMeta();

                // ✅ 핵심: ETF_ID == ISIN_CD
                meta.setEtfId(etfId);
                meta.setIsinCd(etfId);

                meta.setName(t[1].trim());
                meta.setTheme(t[2].trim());
                meta.setAssetType(t[3].trim());
                meta.setRiskLevel(parseInt(t[4]));
                meta.setExpenseLevel(t[5].trim());

                etfMetaRepository.save(meta);
                saved++;
            }
        }

        System.out.println("=================================");
        System.out.println("ETF_META mock data loaded");
        System.out.println("saved   : " + saved);
        System.out.println("skipped : " + skipped);
        System.out.println("=================================");
    }

    private Integer parseInt(String v) {
        try {
            return Integer.valueOf(v.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
