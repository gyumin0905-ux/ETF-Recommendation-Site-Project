package com.example.etfsj.init;

import com.example.etfsj.domain.Term;
import com.example.etfsj.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class TermExcelLoader implements CommandLineRunner {

    private final TermRepository termRepository;

    @Override
    public void run(String... args) throws Exception {

        if (termRepository.count() > 0) {
            System.out.println("[TermExcelLoader] 이미 로딩됨 → 스킵");
            return;
        }

        ClassPathResource resource =
                new ClassPathResource("data/ETF_기초_투자_용어_정리.xlsx");

        DataFormatter formatter = new DataFormatter();
        int saved = 0;

        try (InputStream is = resource.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();

            for (int i = 1; i < rows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String termName = formatter.formatCellValue(row.getCell(1)).trim();
                String descRaw = formatter.formatCellValue(row.getCell(2));

                if (termName.isBlank()) {
                    System.out.println("[SKIP] row " + i + " : 용어 없음");
                    continue;
                }

                // 🔥 핵심: NULL 절대 금지
                String description = (descRaw == null || descRaw.isBlank())
                        ? "설명이 준비 중인 용어입니다."
                        : descRaw.trim();

                Term term = Term.builder()
                        .name(termName)
                        .description(description) // ✅ NOT NULL 보장
                        .detail("")               // 빈 문자열 OK
                        .build();

                termRepository.save(term);
                saved++;

                System.out.println("[LOAD] " + termName);
            }
        }

        System.out.println("[TermExcelLoader] ETF 용어 엑셀 로딩 완료 (" + saved + "건)");
    }
}
