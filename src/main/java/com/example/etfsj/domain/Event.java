package com.example.etfsj.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "EVENT")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EVENT_SEQ_GEN")
    @SequenceGenerator(name = "EVENT_SEQ_GEN", sequenceName = "EVENT_SEQ", allocationSize = 1)
    @Column(name = "event_id")
    private Long id;

    private String title;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "link_url")
    private String linkUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // [규민] 현재 날짜 기준으로 진행 중인지 확인하는 편의 메서드
    public String getStatus() {
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) {
            return "SCHEDULED"; // 예정
        } else if (today.isAfter(endDate)) {
            return "ENDED";     // 종료
        } else {
            return "ING";       // 진행중
        }
    }
}