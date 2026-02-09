package com.example.etfsj.controller;

import com.example.etfsj.service.TermChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TermChatController {

    private final TermChatService termChatService;

    @GetMapping("/terms/chat")
    public String termChatPage(Model model) {
        List<String> terms = termChatService.getAllTermNames();

        // 🔍 디버깅용 (콘솔 확인)
        System.out.println("🔥 용어 개수 = " + terms.size());
        System.out.println("🔥 용어 목록 = " + terms);

        model.addAttribute("terms", terms);
        return "terms-chat";
    }

    @PostMapping("/terms/ask")
    @ResponseBody
    public String ask(@RequestParam("question") String question) {
        return termChatService.answer(question);
    }
}
