package com.example.etfsj.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "BOARD")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BOARD_SEQ_GENERATOR")
    @SequenceGenerator(
            name = "BOARD_SEQ_GENERATOR",
            sequenceName = "BOARD_SEQ", // DB에 이 이름으로 번호표 기계가 생김
            initialValue = 1,
            allocationSize = 1
    )
    @Column(name = "board_id")
    private Long id; // 게시글 PK

    @Column(length = 200, nullable = false)
    private String title; // 게시글 제목

    @Lob  //
    @Column(nullable = false)
    private String content; // 게시글 내용

    @Column(columnDefinition = "integer default 0")
    private int views; // 조회수

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate; // 작성일시 (INSERT 시 자동 생성)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 작성자 (User 엔티티와 다대일 연관관계)

    // [규민] 좋아요 기능 추가 (N:M 관계, 중복 방지를 위해 Set 사용)
    @ManyToMany
    @JoinTable(
            name = "BOARD_LIKES", // 중간 테이블 이름
            joinColumns = @JoinColumn(name = "board_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likes = new HashSet<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // ✅ 이거 추가
    @Transient
    public int getLikesCount() {
        return likes == null ? 0 : likes.size();
    }
}