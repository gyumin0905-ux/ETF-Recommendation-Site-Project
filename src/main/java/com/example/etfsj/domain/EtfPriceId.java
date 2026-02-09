package com.example.etfsj.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EtfPriceId implements Serializable {

    private String basDt;
    private String isinCd;
}