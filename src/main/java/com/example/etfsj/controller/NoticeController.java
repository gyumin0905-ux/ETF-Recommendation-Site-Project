package com.example.etfsj.controller;

import com.example.etfsj.domain.Notice;
import com.example.etfsj.domain.User;
import com.example.etfsj.service.NoticeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 🔥 [수정] 1. 목록 (검색 키워드 받기)
    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        List<Notice> notices = noticeService.getNoticeList(keyword);
        model.addAttribute("notices", notices);
        model.addAttribute("keyword", keyword); // 검색어 유지용
        return "notice/list";
    }

    // 2. 글쓰기 폼
    @GetMapping("/write")
    public String writeForm(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null || !user.isAdmin()) return "redirect:/";
        return "notice/form";
    }

    // 3. 저장
    @PostMapping("/save")
    public String save(@RequestParam("newsLink") String newsLink, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null || !user.isAdmin()) return "redirect:/";

        noticeService.createNewsFromLink(newsLink, user.getId());
        return "redirect:/notice";
    }

    // 4. 상세 보기
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Notice notice = noticeService.getNotice(id);
        model.addAttribute("notice", notice);
        return "notice/detail";
    }

    // 5. [규민] 삭제 기능 (관리자만)
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");

        // 관리자가 아니면 목록으로 쫓아냄
        if (user == null || !user.isAdmin()) {
            return "redirect:/notice";
        }

        noticeService.deleteNotice(id);
        return "redirect:/notice"; // 삭제 후 목록으로 이동
    }
}