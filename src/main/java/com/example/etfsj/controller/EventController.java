package com.example.etfsj.controller;

import com.example.etfsj.domain.Event;
import com.example.etfsj.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // 1. 이벤트 목록 페이지
    @GetMapping
    public String list(Model model) {
        List<Event> events = eventService.getEventList();

        // 전체, 진행중, 종료 개수 계산
        long totalCount = events.size();
        long ingCount = events.stream().filter(e -> "ING".equals(e.getStatus())).count();
        long endCount = events.stream().filter(e -> "ENDED".equals(e.getStatus())).count();

        model.addAttribute("events", events);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("ingCount", ingCount);
        model.addAttribute("endCount", endCount);

        return "event/list";
    }

    // 2. [추가됨] 이벤트 상세 페이지
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Event event = eventService.getEvent(id); // 서비스에 getEvent 메서드 있다고 가정
        model.addAttribute("event", event);
        return "event/detail";
    }
}