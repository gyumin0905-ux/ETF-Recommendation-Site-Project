package com.example.etfsj.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "etf")
public class EtfProperties {

    private Recommend recommend = new Recommend();

    @Getter
    @Setter
    public static class Recommend {
        private int defaultTopN = 5;
    }
}
