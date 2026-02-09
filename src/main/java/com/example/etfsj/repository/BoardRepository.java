package com.example.etfsj.repository;

import com.example.etfsj.domain.Board;
import com.example.etfsj.domain.BoardListDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 전체 글 최신순 조회
    @Query("SELECT b FROM Board b JOIN FETCH b.user ORDER BY b.id DESC")
    List<Board> findAllByOrderByIdDesc();

    // 상세 조회 (작성자 + 좋아요 정보 포함)
    @Query("SELECT b FROM Board b JOIN FETCH b.user LEFT JOIN FETCH b.likes WHERE b.id = :id")
    Optional<Board> findByIdWithUserAndLikes(@Param("id") Long id);

    // [규민] 내가 쓴 글 목록 조회 (마이페이지용) - 최신순 정렬
    List<Board> findAllByUserIdOrderByIdDesc(Long userId);

    // 게시판 목록 DTO (조회수 / 좋아요 안정화)
    @Query("""
        select new com.example.etfsj.domain.BoardListDto(
            b.id,
            b.title,
            u.username,
            b.createdDate,
            b.views,
            size(b.likes)
        )
        from Board b
        left join b.user u
        order by b.id desc
    """)
    List<BoardListDto> findBoardListDto();

    // 🔥 최근 게시글 (user fetch 필수)
    @Query("""
        select b from Board b
        join fetch b.user
        order by b.id desc
    """)
    List<Board> findRecentBoards();
}