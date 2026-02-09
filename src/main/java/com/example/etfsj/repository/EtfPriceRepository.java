package com.example.etfsj.repository;

import com.example.etfsj.domain.EtfPrice;
import com.example.etfsj.domain.EtfPriceId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EtfPriceRepository
        extends JpaRepository<EtfPrice, EtfPriceId> {

    // 🔥 평균 거래대금 (추천용)
    @Query(value = """
        SELECT
            ISIN_CD,
            AVG(TR_PRC) AS avgTrPrc
        FROM ETF_PRICE
        WHERE BAS_DT >= :fromDt
          AND TR_PRC IS NOT NULL
        GROUP BY ISIN_CD
        ORDER BY avgTrPrc DESC
    """, nativeQuery = true)
    List<Object[]> findAvgTradeAmount(@Param("fromDt") String fromDt);

    // ===============================
    // 🔥 특정 ETF 최신 기준일 (차트용)
    // ===============================
    @Query("""
        SELECT MAX(e.basDt)
        FROM EtfPrice e
        WHERE e.isinCd = :isin
    """)
    String findLatestBasDt(@Param("isin") String isin);

    // ===============================
    // 🔥 전체 ETF 최신 기준일 (추천용 ⭐ 추가)
    // ===============================
    // 🔥 전체 ETF 중 최신 기준일
    @Query("""
        SELECT MAX(e.basDt)
        FROM EtfPrice e
    """)
    String findGlobalLatestBasDt();

    // ===============================
    // 🔥 기간별 가격 데이터 (차트용)
    // ===============================
    @Query("""
        SELECT e
        FROM EtfPrice e
        WHERE e.isinCd = :isin
          AND e.basDt >= :fromDt
        ORDER BY e.basDt
    """)
    List<EtfPrice> findByPeriod(
            @Param("isin") String isin,
            @Param("fromDt") String fromDt
    );

    @Query(value = """
    SELECT
        p.ISIN_CD,
        AVG(p.TR_PRC) AS avgTrPrc
    FROM ETF_PRICE p
    WHERE p.BAS_DT >= :fromDt
    GROUP BY p.ISIN_CD
    ORDER BY avgTrPrc DESC
""", nativeQuery = true)
    List<Object[]> findPopularAll(@Param("fromDt") String fromDt);

    @Query("""
        SELECT p FROM EtfPrice p
        WHERE p.isinCd = :isin
        ORDER BY p.basDt DESC
    """)
    List<EtfPrice> findRecentPrices(String isin, Pageable pageable);

    /**
     * 최근 생긴 ETF 데이터를 기준으로 5개 출력
     * @return
     */
    List<EtfPrice> findTop5ByOrderByBasDtDesc();

    // ===============================
    // 🔥 관리자 페이지
    // 🔥 일 단위 날짜 검색 + 페이징 (YYYYMMDD 기준)
    // ===============================
    @Query("""
        SELECT e
        FROM EtfPrice e
        WHERE e.basDt BETWEEN :startDt AND :endDt
        ORDER BY e.basDt DESC
    """)
    Page<EtfPrice> findByBasDtBetween(
            @Param("startDt") String startDt,
            @Param("endDt") String endDt,
            Pageable pageable
    );

    // 🔥 ISIN 단독 검색
    Page<EtfPrice> findByIsinCd(String isinCd, Pageable pageable);

    // 🔥 날짜 + ISIN 검색
    @Query("""
    SELECT e FROM EtfPrice e
    WHERE e.basDt BETWEEN :startDt AND :endDt
      AND e.isinCd = :isin
    ORDER BY e.basDt DESC
""")
    Page<EtfPrice> findByBasDtBetweenAndIsin(
            @Param("startDt") String startDt,
            @Param("endDt") String endDt,
            @Param("isin") String isin,
            Pageable pageable
    );

    // 날짜 + ISIN
    Page<EtfPrice> findByBasDtBetweenAndIsinCd(
            String startDt,
            String endDt,
            String isinCd,
            Pageable pageable
    );

    // 최근 N개 가격 조회
    @Query("""
        select p
        from EtfPrice p
        where p.isinCd = :isinCd
        order by p.basDt desc
    """)
    List<EtfPrice> findRecentPrice(
            @Param("isinCd") String isinCd,
            Pageable pageable
    );

    @Query("""
    SELECT ep
    FROM EtfPrice ep
    WHERE ep.itmsNm IN :names
      AND ep.basDt = (
          SELECT MAX(ep2.basDt)
          FROM EtfPrice ep2
          WHERE ep2.itmsNm = ep.itmsNm
      )
""")
    List<EtfPrice> findBannerEtfs(@Param("names") List<String> names);
}
