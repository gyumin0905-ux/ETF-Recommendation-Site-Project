package com.example.etfsj.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "NOTICE")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NOTICE_SEQ_GENERATOR")
    @SequenceGenerator(
            name = "NOTICE_SEQ_GENERATOR",
            sequenceName = "NOTICE_SEQ", // DB에 생길 시퀀스 이름
            initialValue = 1,
            allocationSize = 1
    )
    @Column(name = "notice_id")
    private Long id;

    @Column(length = 200, nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "integer default 0")
    private int views;

    // [규민] 뉴스 기능을 위해 추가된 필드: 원본 기사 링크
    private String originalUrl;

    // [규민] 뉴스 기능을 위해 추가된 필드: 기사 썸네일 이미지 주소
    private String thumbnailUrl;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // [규민] 뉴스 생성 편의 메서드 (서비스에서 호출)
    public static Notice createNews(String title, String content, String originalUrl, String thumbnailUrl, User user) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setOriginalUrl(originalUrl);
        notice.setThumbnailUrl(thumbnailUrl);
        notice.setUser(user);
        notice.setViews(0);
        // createdDate는 @CreationTimestamp가 있어서 자동 생성됨
        return notice;
    }
}