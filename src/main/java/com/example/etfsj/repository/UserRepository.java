package com.example.etfsj.repository;

import com.example.etfsj.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일 중복 체크용
     * 생성될 쿼리 예(개념):
     * SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
     * FROM USERS WHERE EMAIL = ?
     */
    boolean existsByEmail(String email);

    /**
     * [추가] 아이디(username) 중복 체크용
     * SELECT COUNT(*) FROM USERS WHERE USERNAME = ?
     */
    boolean existsByUsername(String username);

    /**
     * 이메일로 사용자 조회
     * 생성될 쿼리 예(개념):
     * SELECT * FROM USERS WHERE EMAIL = ?
     */
    Optional<User> findByEmail(String email);


    /**
     *
     * @param email
     * @param provider
     * @return
     */
    Optional<User> findByEmailAndProvider(String email, String provider);

    /**
     *
     * @param provider
     * @param providerId
     * @return
     * 카카오 , 구글 , 네이버
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    /**
     * 최근 생긴 유저를 기준으로 5개 출력
     * @return
     */
    List<User> findTop5ByOrderByIdDesc();

    @Query("""
        SELECT u FROM User u
        WHERE (:keyword IS NULL
            OR CAST(u.id AS string) LIKE %:keyword%
            OR u.username LIKE %:keyword%
            OR u.email LIKE %:keyword%)
        AND (:role IS NULL OR u.userRole = :role)
        AND (:risk IS NULL OR u.risk = :risk)
    """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") String role,
            @Param("risk") String risk,
            Pageable pageable
    );
}
