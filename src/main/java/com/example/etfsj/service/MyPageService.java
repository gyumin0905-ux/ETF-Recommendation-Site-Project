package com.example.etfsj.service;

import com.example.etfsj.domain.EtfBookmark;
import com.example.etfsj.domain.EtfMeta;
import com.example.etfsj.repository.EtfBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final EtfBookmarkRepository bookmarkRepository;
    private final EtfSearchService etfSearchService;

    public List<EtfMeta> getBookmarkedEtfs(Long userId) {

        // 1️⃣ 북마크된 ETF ID 목록
        List<String> etfIds = bookmarkRepository.findByUserId(userId)
                .stream()
                .map(EtfBookmark::getEtfId)
                .toList();

        // 2️⃣ ETF 메타 조회
        return etfIds.stream()
                .map(etfSearchService::findByIsin)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
