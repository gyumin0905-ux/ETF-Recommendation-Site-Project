package com.example.etfsj.repository;

import com.example.etfsj.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // 종료일이 늦은 순(최신 이벤트 위로), 그 다음엔 시작일 순으로 정렬
    List<Event> findAllByOrderByEndDateDescStartDateDesc();

    /**
     * 최근 생긴 이벤트를 기준으로 5개 출력
     * @return
     */
    List<Event> findTop5ByOrderByIdDesc();

    @Query("""
        SELECT e FROM Event e
        WHERE
            (:keyword IS NULL OR e.title LIKE %:keyword%)
        AND (:startDate IS NULL OR e.startDate >= :startDate)
        AND (:endDate IS NULL OR e.endDate <= :endDate)
        ORDER BY e.startDate DESC
    """)
    Page<Event> searchEvents(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}