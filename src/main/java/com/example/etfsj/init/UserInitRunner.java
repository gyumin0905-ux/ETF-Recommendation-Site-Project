package com.example.etfsj.init;

import com.example.etfsj.domain.User;
import com.example.etfsj.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserInitRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInitRunner(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        createUserIfNotExists(
                "admin@gmail.com",
                "ADMIN"
        );

        createUserIfNotExists(
                "user@gmail.com",
                "USER"
        );
    }

    private void createUserIfNotExists(String email, String role) {

        if (userRepository.existsByEmail(email)) {
            return; // ✅ 이미 있으면 아무 것도 안 함
        }

        String rawPassword = "1234"; // 🔥 개발용 고정 비밀번호

        User user = new User();
        user.setUsername(role.toLowerCase());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setUserRole(role);

        // 기본 성향값
        user.setExperience("beginner");
        user.setAsset("etf");
        user.setGoal("mid_term");
        user.setRisk("neutral");
        user.setKnowledge("basic");

        userRepository.save(user);

        // 🔥 콘솔 출력
        System.out.println("====================================");
        System.out.println("✅ 초기 계정 생성됨");
        System.out.println("ROLE     : " + role);
        System.out.println("EMAIL    : " + email);
        System.out.println("PASSWORD : " + rawPassword);
        System.out.println("====================================");
    }
}
