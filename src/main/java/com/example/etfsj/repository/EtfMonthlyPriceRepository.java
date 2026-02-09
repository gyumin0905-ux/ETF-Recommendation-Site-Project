package com.example.etfsj.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EtfMonthlyPriceRepository {

    @PersistenceContext
    private EntityManager em;

    // 월별 평균 종가
    public List<Object[]> findMonthlyPrices(String isin, String startYm, String endYm) {
        String sql = """
            SELECT
                SUBSTR(BAS_DT, 1, 6) AS month,
                AVG(CLPR) AS price
            FROM ETF_PRICE
            WHERE ISIN_CD = :isin
              AND SUBSTR(BAS_DT, 1, 6) BETWEEN :startYm AND :endYm
            GROUP BY SUBSTR(BAS_DT, 1, 6)
            ORDER BY month
        """;

        return em.createNativeQuery(sql)
                .setParameter("isin", isin)
                .setParameter("startYm", startYm)
                .setParameter("endYm", endYm)
                .getResultList();
    }

    public String findMinMonth(String isin) {
        return (String) em.createNativeQuery("""
            SELECT MIN(SUBSTR(BAS_DT,1,6))
            FROM ETF_PRICE
            WHERE ISIN_CD = :isin
        """).setParameter("isin", isin)
                .getSingleResult();
    }

    public String findMaxMonth(String isin) {
        return (String) em.createNativeQuery("""
            SELECT MAX(SUBSTR(BAS_DT,1,6))
            FROM ETF_PRICE
            WHERE ISIN_CD = :isin
        """).setParameter("isin", isin)
                .getSingleResult();
    }
}
