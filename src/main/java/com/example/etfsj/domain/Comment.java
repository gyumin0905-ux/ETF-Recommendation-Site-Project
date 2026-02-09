package com.example.etfsj.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "BOARD_COMMENT")
@ToString(exclude = {"board", "user"}) // ⭐ 중요: 순환참조 방지
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BOARD_COMMENT_SEQ_GEN")
    @SequenceGenerator(
            name = "BOARD_COMMENT_SEQ_GEN",
            sequenceName = "BOARD_COMMENT_SEQ",
            allocationSize = 1
    )
    @Column(name = "comment_id")
    private Long id;

    @Lob
    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    // 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
