package com.example.etfsj.service;

import com.example.etfsj.domain.Event;
import com.example.etfsj.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public List<Event> getEventList() {
        return eventRepository.findAllByOrderByEndDateDescStartDateDesc();
    }

    // [규민] 이벤트 저장 (생성 및 수정)
    @Transactional
    public void saveEvent(Event event) {
        eventRepository.save(event);
    }

    // [규민] 이벤트 삭제
    @Transactional
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    // [규민] 이벤트 단건 조회 (수정 폼 채우기용)
    public Event getEvent(Long id) {
        return eventRepository.findById(id).orElse(new Event());
    }

    // [세영] 어드민 대시보드에 최근 5개 데이터 조회
    public List<Event> getRecentEvents(int limit) {
        return eventRepository.findTop5ByOrderByIdDesc();
    }

    public Page<Event> searchEvents(
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        return eventRepository.searchEvents(
                keyword, startDate, endDate, pageable
        );
    }
}