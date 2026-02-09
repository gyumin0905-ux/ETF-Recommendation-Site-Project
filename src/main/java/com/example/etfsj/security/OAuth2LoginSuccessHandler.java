package com.example.etfsj.security;

import com.example.etfsj.domain.User;
import com.example.etfsj.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String provider =
                ((OAuth2AuthenticationToken) authentication)
                        .getAuthorizedClientRegistrationId()
                        .toUpperCase();

        String providerId;
        String email;
        String name;

        // ===============================
        // GOOGLE
        // ===============================
        if ("GOOGLE".equals(provider)) {

            providerId = oAuth2User.getAttribute("sub");
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");

        }
        // ===============================
        // KAKAO (이메일 없음 → 가짜 이메일)
        // ===============================
        else if ("KAKAO".equals(provider)) {

            Object idObj = oAuth2User.getAttribute("id");
            providerId = String.valueOf(idObj); // 🔥 핵심

            Map<String, Object> properties =
                    (Map<String, Object>) oAuth2User.getAttribute("properties");

            name = (properties != null && properties.get("nickname") != null)
                    ? properties.get("nickname").toString()
                    : "kakao_user";

            email = "kakao_" + providerId + "@social.kakao";
        }
        // ===============================
        // NAVER
        // ===============================
        else if ("NAVER".equals(provider)) {

            Map<String, Object> responseMap =
                    (Map<String, Object>) oAuth2User.getAttribute("response");

            providerId = responseMap.get("id").toString();

            name = responseMap.get("name") != null
                    ? responseMap.get("name").toString()
                    : "naver_user";

            email = responseMap.get("email") != null
                    ? responseMap.get("email").toString()
                    : "naver_" + providerId + "@social.naver";

        } else {
            throw new IllegalStateException("지원하지 않는 OAuth Provider: " + provider);
        }

        // ===============================
        // User 조회 or 생성
        // ===============================
        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(name);
                    newUser.setProvider(provider);
                    newUser.setProviderId(providerId);
                    newUser.setUserRole("USER");
                    return userRepository.save(newUser);
                });

        // ===============================
        // 세션 로그인 처리
        // ===============================
        request.getSession(true).setAttribute("loginUser", user);

        response.sendRedirect("/");
    }
}
