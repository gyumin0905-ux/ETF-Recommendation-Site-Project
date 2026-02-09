package com.example.etfsj.repository;

import com.example.etfsj.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 목록 조회 (최신순)
    @Query("SELECT n FROM Notice n JOIN FETCH n.user ORDER BY n.id DESC")
    List<Notice> findAllByOrderByIdDesc();

    // 검색 기능
    List<Notice> findByTitleContainingOrContentContainingOrderByIdDesc(String title, String content);

    // 🔥 [핵심 수정] 상세 조회 시 작성자(User) 정보를 'JOIN FETCH'로 미리 가져옴 (에러 해결)
    @Query("SELECT n FROM Notice n JOIN FETCH n.user WHERE n.id = :id")
    Optional<Notice> findByIdWithUser(@Param("id") Long id);

    List<Notice> findTop5ByOrderByCreatedDateDesc();

    List<Notice> findAllByOrderByCreatedDateDesc();
}