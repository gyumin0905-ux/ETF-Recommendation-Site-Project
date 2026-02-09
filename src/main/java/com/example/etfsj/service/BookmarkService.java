package com.example.etfsj.service;

import com.example.etfsj.domain.EtfBookmark;
import com.example.etfsj.repository.EtfBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final EtfBookmarkRepository bookmarkRepository;

    public void toggle(Long userId, String etfId) {

        if (bookmarkRepository.existsByUserIdAndEtfId(userId, etfId)) {
            bookmarkRepository.deleteByUserIdAndEtfId(userId, etfId);
            return;
        }

        bookmarkRepository.save(
                new EtfBookmark(userId, etfId)
        );
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Long userId, String etfId) {
        return bookmarkRepository.existsByUserIdAndEtfId(userId, etfId);
    }
}