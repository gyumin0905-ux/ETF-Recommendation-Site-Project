package com.example.etfsj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "ETF_PRICE")
@IdClass(EtfPriceId.class)
@Getter
@Setter
public class EtfPrice {

    @Id
    @Column(name = "BAS_DT")
    private String basDt;      // YYYY-MM-DD 그대로 사용

    @Id
    @Column(name = "ISIN_CD")
    private String isinCd;

    @Column(name = "ITMS_NM")
    private String itmsNm;   // ✅ 추가

    @Column(name = "CLPR")
    private Double clpr;

    @Column(name = "VS")
    private Long vs;

    @Column(name = "FLUC_RT")
    private Double flucRt;

    @Column(name = "MKP")
    private Double mkp;

    @Column(name = "HIPR")
    private Double hipr;       // 고가

    @Column(name = "LOPR")
    private Double lopr;       // 저가

    @Column(name = "TR_QUANT")
    private Long trQuant;

    @Column(name = "TR_PRC")
    private Long trPrc;
}
