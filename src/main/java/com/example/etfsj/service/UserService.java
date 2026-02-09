package com.example.etfsj.service;

import com.example.etfsj.domain.User;
import com.example.etfsj.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * UserService
 *
 * 역할:
 * - 회원가입
 * - 로그인
 * - 설문 데이터 업데이트
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===============================
    // 회원가입 (화면용 – 현재 미사용)
    // ===============================
    public void signup(String username, String email, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
    }

    // ===============================
    // 🔥 회원가입 + userId 반환 (팝업용)
    // ===============================
    public User signupAndReturn(String username, String email, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        // ⭐ 기본값 직접 세팅 (핵심)
        user.setExperience("beginner");
        user.setAsset("etf");
        user.setGoal("mid_term");
        user.setRisk("neutral");
        user.setKnowledge("basic");

        return userRepository.save(user);
    }

    // ===============================
    // 로그인
    // ===============================
    @Transactional(readOnly = true)
    public User login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 이메일입니다.")
                );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        return user;
    }

    // ===============================
    // 🔥 설문 데이터 업데이트
    // ===============================
    public void updateSurvey(
            Long userId,
            String experience,
            String asset,
            String goal,
            String risk,
            String knowledge
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다.")
                );

        user.setExperience(experience);
        user.setAsset(asset);
        user.setGoal(goal);
        user.setRisk(risk);
        user.setKnowledge(knowledge);
        // save() 필요 없음 (JPA dirty checking)
    }

    // ===============================
    // 🔥 유저 정보 수정 (현재 사용 안함)
    // ===============================
    public void updateUser(Long id,
                           String username,
                           String email,
                           String password,
                           String experience,
                           String asset,
                           String goal,
                           String risk,
                           String knowledge) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // createdAt 유지
        LocalDateTime created = user.getCreatedAt();
        String role = user.getUserRole();

        user.setUsername(username);
        user.setEmail(email);
        // ⭐ 핵심: 비밀번호는 입력된 경우에만 변경
        if (password != null && !password.isBlank()) {
            user.setPassword(password);
        }
        user.setExperience(experience);
        user.setAsset(asset);
        user.setGoal(goal);
        user.setRisk(risk);
        user.setKnowledge(knowledge);

        // 다시 복구
        user.setCreatedAt(created);
        user.setUserRole(role);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // ===============================
    // 🔥 유저 정보 수정, 마이페이지, 비밀번호 확인 로직 추가
    // ===============================
    public void updateUserWithPasswordCheck(
            Long id,
            String username,
            String email,

            String currentPassword,
            String newPassword,
            String confirmPassword,

            String experience,
            String asset,
            String goal,
            String risk,
            String knowledge
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // ===============================
        // 🔥 SOCIAL 계정이면 비밀번호 로직 완전 스킵
        // ===============================
        boolean isLocalUser = "LOCAL".equals(user.getProvider());

        // ===============================
        // 1️⃣ 기본 정보 / 투자 성향 수정 (공통)
        // ===============================
        user.setUsername(username);
        user.setEmail(email);
        user.setExperience(experience);
        user.setAsset(asset);
        user.setGoal(goal);
        user.setRisk(risk);
        user.setKnowledge(knowledge);

        // ===============================
        // 2️⃣ LOCAL 계정만 비밀번호 변경 허용
        // ===============================
        if (!isLocalUser) {
            return; // 🔥 여기서 종료
        }

        // ===============================
        // 3️⃣ LOCAL 계정 비밀번호 검증
        // ===============================
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("CURRENT_PASSWORD_REQUIRED");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("CURRENT_PASSWORD_MISMATCH");
        }

        boolean newPwEmpty = (newPassword == null || newPassword.trim().isEmpty());
        boolean confirmPwEmpty = (confirmPassword == null || confirmPassword.trim().isEmpty());

        if (newPwEmpty && confirmPwEmpty) {
            return;
        }

        if (newPwEmpty || confirmPwEmpty) {
            throw new IllegalArgumentException("NEW_PASSWORD_INVALID");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("NEW_PASSWORD_INVALID");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
    }

}
