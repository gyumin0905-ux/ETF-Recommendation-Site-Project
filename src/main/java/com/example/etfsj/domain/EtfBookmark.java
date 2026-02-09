package com.example.etfsj.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "ETF_BOOKMARK",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"USER_ID", "ETF_ID"})
        }
)
public class EtfBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "ETF_ID", nullable = false)
    private String etfId;

    public EtfBookmark(Long userId, String etfId) {
        this.userId = userId;
        this.etfId = etfId;
    }
}
