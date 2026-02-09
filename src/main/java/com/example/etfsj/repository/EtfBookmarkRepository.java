package com.example.etfsj.repository;

import com.example.etfsj.domain.EtfBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtfBookmarkRepository extends JpaRepository<EtfBookmark, Long> {

    boolean existsByUserIdAndEtfId(Long userId, String etfId);

    void deleteByUserIdAndEtfId(Long userId, String etfId);

    List<EtfBookmark> findByUserId(Long userId);
}
