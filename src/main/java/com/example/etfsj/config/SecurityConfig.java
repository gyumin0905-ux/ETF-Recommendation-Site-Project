package com.example.etfsj.config;

import com.example.etfsj.security.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig(OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CSRF 끔 (개발 단계)
                .csrf(csrf -> csrf.disable())

                // 🔥 iframe 허용 (같은 origin)
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin())
                )

                // 모든 요청 허용 (세션 기반 로그인 직접 구현 중)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // 기본 로그인 폼 비활성화
                .formLogin(form -> form.disable())

                // 기본 HTTP Basic 유지 (기존 기능 유지)
                .httpBasic(Customizer.withDefaults())

                // 🔥 OAuth2 로그인 추가 (핵심)
                .oauth2Login(oauth -> oauth
                        .loginPage("/login") // 기존 로그인 페이지 재사용
                        .successHandler(oAuth2LoginSuccessHandler)
                )

                // 🔥 로그아웃 설정 추가 (핵심)
                .logout(logout -> logout
                        .logoutUrl("/logout")        // 로그아웃 URL
                        .logoutSuccessUrl("/")       // 🔥 메인 페이지로 이동
                        .invalidateHttpSession(true) // 세션 제거
                        .clearAuthentication(true)   // 인증 정보 제거
                );

        return http.build();
    }
}