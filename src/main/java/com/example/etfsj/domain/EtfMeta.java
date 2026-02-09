package com.example.etfsj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ETF_META")
@Getter @Setter
public class EtfMeta {

    @Id
    @Column(name = "ISIN_CD")
    private String isinCd;   // ✅ PK = ISIN

    @Column(name = "ETF_ID")
    private String etfId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "THEME")
    private String theme;

    @Column(name = "ASSET_TYPE")
    private String assetType;

    @Column(name = "RISK_LEVEL")
    private Integer riskLevel;

    @Column(name = "EXPENSE_LEVEL")
    private String expenseLevel;
}
