package com.example.etfsj.config;

import com.example.etfsj.domain.EtfPrice;
import com.example.etfsj.domain.EtfPriceId;
import com.example.etfsj.repository.EtfPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "etf.csv",
        name = "load-price",
        havingValue = "true"
)
public class EtfPriceCsvLoader implements CommandLineRunner {

    private static final int BATCH_SIZE = 500;

    private final EtfPriceRepository etfPriceRepository;

    @Override
    public void run(String... args) throws Exception {

        // ✅ 이미 데이터 있으면 스킵 (재시작 시 속도 보호)
        if (etfPriceRepository.count() > 0) {
            System.out.println("ETF_PRICE already loaded. skip.");
            return;
        }

        ClassPathResource resource =
                new ClassPathResource("data/ETF_PRICE_TOTAL_COMBINED.csv");

        List<EtfPrice> buffer = new ArrayList<>(BATCH_SIZE);

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

                // 3️⃣ 컬럼 개수 방어 (최소 11개 필요)
                if (t.length < 11) {
                    skipped++;
                    continue;
                }

                String basDt  = t[0].trim(); // BAS_DT
                String isinCd = t[1].trim(); // ISIN_CD

                // 4️⃣ PK null 방어
                if (basDt.isEmpty() || isinCd.isEmpty()) {
                    skipped++;
                    continue;
                }

                EtfPriceId id = new EtfPriceId(basDt, isinCd);

                // 5️⃣ 중복 방어
                if (etfPriceRepository.existsById(id)) {
                    skipped++;
                    continue;
                }

                EtfPrice price = new EtfPrice();
                price.setBasDt(basDt);
                price.setIsinCd(isinCd);

                // 6️⃣ 컬럼 매핑 (CSV 구조 기준)
                // BAS_DT | ISIN_CD | ITMS_NM | CLPR | VS | FLUC_RT | MKP | HIPR | LOPR | TR_QUANT | TR_PRC
                price.setItmsNm(t[2].trim());          // 종목명
                price.setClpr(parseDouble(t[3]));      // 종가
                price.setVs(parseLong(t[4]));          // 전일 대비
                price.setFlucRt(parseDouble(t[5]));    // 등락률
                price.setMkp(parseDouble(t[6]));       // 시가
                price.setHipr(parseDouble(t[7]));      // 고가
                price.setLopr(parseDouble(t[8]));      // 저가
                price.setTrQuant(parseLong(t[9]));     // 거래량
                price.setTrPrc(parseLong(t[10]));      // 거래대금

                buffer.add(price);

                // 7️⃣ 배치 저장
                if (buffer.size() >= BATCH_SIZE) {
                    etfPriceRepository.saveAll(buffer);
                    saved += buffer.size();
                    buffer.clear();
                }
            }

            // 8️⃣ 남은 데이터 저장
            if (!buffer.isEmpty()) {
                etfPriceRepository.saveAll(buffer);
                saved += buffer.size();
            }
        }

        System.out.println("=================================");
        System.out.println("ETF_PRICE CSV 적재 완료");
        System.out.println("saved   : " + saved);
        System.out.println("skipped : " + skipped);
        System.out.println("=================================");
    }

    private Double parseDouble(String v) {
        try {
            return Double.valueOf(v.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String v) {
        try {
            return Long.valueOf(v.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
