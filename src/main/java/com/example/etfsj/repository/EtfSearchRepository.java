package com.example.etfsj.repository;

import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.dto.EtfListDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EtfSearchRepository extends JpaRepository<EtfMeta, String> {
    @Query("""
    SELECT e FROM EtfMeta e
    WHERE (:keyword IS NULL OR
       e.name LIKE %:keyword% OR
       e.theme LIKE %:keyword%)
    AND (:theme IS NULL OR e.theme = :theme)
    AND (:riskLevel IS NULL OR e.riskLevel = :riskLevel)
    AND (:expenseLevel IS NULL OR e.expenseLevel = :expenseLevel)
    """)
    List<EtfMeta> search(
            @Param("keyword") String keyword,
            @Param("theme") String theme,
            @Param("riskLevel") Integer riskLevel,
            @Param("expenseLevel") String expenseLevel
    );

    @Query("""
            select new com.example.etfsj.dto.EtfListDto(
                m.isinCd,
                m.name,
                m.theme,
                m.riskLevel,
                p.trPrc,
                p.flucRt,
                null
            )
            from EtfMeta m
            left join EtfPrice p
                on m.isinCd = p.isinCd
                and p.basDt = (
                    select max(p2.basDt)
                    from EtfPrice p2
                    where p2.isinCd = m.isinCd
                )
            where (:keyword is null
                    or m.name like %:keyword%
                    or m.theme like %:keyword%)
            and (:theme is null or m.theme = :theme)
            and (:riskLevel is null or m.riskLevel = :riskLevel)
            and (:expenseLevel is null or m.expenseLevel = :expenseLevel)
            """)
    List<EtfListDto> searchWithPrice(
            @Param("keyword") String keyword,
            @Param("theme") String theme,
            @Param("riskLevel") Integer riskLevel,
            @Param("expenseLevel") String expenseLevel
    );

}
