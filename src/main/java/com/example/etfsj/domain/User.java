package com.example.etfsj.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USERS")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    // [규민] SQL의 USER_ROLE 컬럼과 이름 매핑 (기존 role -> userRole 변경)
    @Column(name = "USER_ROLE")
    private String userRole;
    private String experience;
    private String asset;
    private String goal;
    private String risk;
    private String knowledge;
    // [규민] SQL 테이블에 있는 가입일(CREATED_AT) 컬럼 매핑 추가
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    // [규민] 내가 좋아요 누른 게시글 목록 (양방향 매핑)
    @ManyToMany(mappedBy = "likes")
    private Set<Board> likedBoards = new HashSet<>();

    @Column(name = "PROVIDER")
    private String provider;

    @Column(name = "PROVIDER_ID")
    private String providerId;

    /**
     * [규민] 관리자 여부 확인 메서드
     * DB의 USER_ROLE 값이 'ADMIN'이면 true 반환
     */
    public boolean isAdmin() {
        return "ADMIN".equals(this.userRole);
    }

    // ⭐ JPA 디폴트 수정 안될 때 사용
    @PrePersist
    public void prePersist() {
        if (this.userRole == null) {
            this.userRole = "USER";
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.experience == null) {
            this.experience = "beginner";
        }
        if (this.asset == null) {
            this.asset = "etf";
        }
        if (this.goal == null) {
            this.goal = "mid_term";
        }
        if (this.risk == null) {
            this.risk = "neutral";
        }
        if (this.knowledge == null) {
            this.knowledge = "basic";
        }
        if (this.provider == null) {
            this.provider = "LOCAL";
        }
    }
}
