package com.example.etfsj.controller;

import com.example.etfsj.domain.User;
import com.example.etfsj.service.BookmarkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{etfId}")
    public void toggle(
            @PathVariable String etfId,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new RuntimeException("LOGIN_REQUIRED");
        }

        bookmarkService.toggle(loginUser.getId(), etfId);
    }

    @GetMapping("/{etfId}")
    public BookmarkResponse isBookmarked(
            @PathVariable String etfId,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute("loginUser");

        boolean bookmarked = false;
        if (loginUser != null) {
            bookmarked = bookmarkService.isBookmarked(loginUser.getId(), etfId);
        }

        return new BookmarkResponse(bookmarked);
    }

    // ⭐ DTO
    static class BookmarkResponse {
        public boolean bookmarked;
        public BookmarkResponse(boolean bookmarked) {
            this.bookmarked = bookmarked;
        }
    }
}