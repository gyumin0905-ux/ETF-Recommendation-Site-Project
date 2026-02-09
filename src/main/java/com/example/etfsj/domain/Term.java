package com.example.etfsj.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ETF_TERM")
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TERM_NAME", nullable = false, unique = true, length = 200)
    private String name;

    // 🔥 여기 핵심
    @Column(name = "TERM_DESC", nullable = false, length = 4000)
    private String description;

    @Column(name = "DETAIL", length = 4000)
    private String detail;
}
