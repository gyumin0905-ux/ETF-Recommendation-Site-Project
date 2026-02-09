package com.example.etfsj.repository;

import com.example.etfsj.domain.EtfMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EtfMetaRepository extends JpaRepository<EtfMeta, String> {
    // PK = ISIN_CD (String)

    // [규민] 조건 검색
    @Query("""
        SELECT e FROM EtfMeta e WHERE
            (:theme IS NULL OR e.theme = :theme)
        AND (:riskLevel IS NULL OR e.riskLevel = :riskLevel)
        AND (:expenseLevel IS NULL OR e.expenseLevel = :expenseLevel)
    """)
    List<EtfMeta> searchEtfs(
            @Param("theme") String theme,
            @Param("riskLevel") Integer riskLevel,
            @Param("expenseLevel") String expenseLevel
    );

    // SAFE 추천용: 위험도 낮은 ETF 상위 10개
    List<EtfMeta> findTop10ByRiskLevelLessThanEqualOrderByRiskLevelAsc(Integer riskLevel);

    List<EtfMeta> findAll();

    @Query("SELECT DISTINCT e.theme FROM EtfMeta e WHERE e.theme IS NOT NULL")
    List<String> findDistinctThemes();

    List<EtfMeta> findByTheme(String theme);

    Optional<EtfMeta> findByIsinCd(String isin);

    /**
     * 최근 생긴 메타 데이터를 기준으로 5개 출력
     * @return
     */
    List<EtfMeta> findTop5ByOrderByIsinCdDesc();

    // ===============================
    // 🔥 관리자 페이지용 (페이징 지원)
    // ===============================
    @Query("""
        SELECT e FROM EtfMeta e WHERE
            (:theme IS NULL OR e.theme = :theme)
        AND (:riskLevel IS NULL OR e.riskLevel = :riskLevel)
        AND (:expenseLevel IS NULL OR e.expenseLevel = :expenseLevel)
    """)
    Page<EtfMeta> searchEtfs(
            @Param("theme") String theme,
            @Param("riskLevel") Integer riskLevel,
            @Param("expenseLevel") String expenseLevel,
            Pageable pageable
    );

    @Query("""
    SELECT e FROM EtfMeta e
    WHERE (:keyword IS NULL OR
           e.etfId LIKE %:keyword% OR
           e.name LIKE %:keyword% OR
           e.isinCd LIKE %:keyword%)
      AND (:theme IS NULL OR e.theme = :theme)
      AND (:riskLevel IS NULL OR e.riskLevel = :riskLevel)
      AND (:expenseLevel IS NULL OR e.expenseLevel = :expenseLevel)
""")
    Page<EtfMeta> searchEtfDetail(
            @Param("keyword") String keyword,
            @Param("theme") String theme,
            @Param("riskLevel") Integer riskLevel,
            @Param("expenseLevel") String expenseLevel,
            Pageable pageable
    );
}
