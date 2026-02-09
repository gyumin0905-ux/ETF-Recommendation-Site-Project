package com.example.etfsj.service;

import com.example.etfsj.domain.Board;
import com.example.etfsj.domain.BoardListDto;
import com.example.etfsj.domain.User;
import com.example.etfsj.repository.BoardRepository;
import com.example.etfsj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // ✅ 1. 게시글 목록 조회 (DTO / 최신순)
    public List<BoardListDto> getBoardList() {
        return boardRepository.findAllByOrderByIdDesc()
                .stream()
                .map(BoardListDto::from)
                .toList();
    }

    // 2. 게시글 저장 (글쓰기)
    @Transactional
    public void writeBoard(String title, String content, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        Board board = new Board();
        board.setTitle(title);
        board.setContent(content);
        board.setUser(user);

        boardRepository.save(board);
    }

    // 3. 게시글 상세 조회 (user + likes fetch)
    @Transactional
    public Board getBoard(Long id) {
        return boardRepository.findByIdWithUserAndLikes(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 없습니다."));
    }

    // 4. 좋아요 토글
    @Transactional
    public void toggleLike(Long boardId, Long userId) {
        Board board = boardRepository.findByIdWithUserAndLikes(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        if (board.getLikes().stream().anyMatch(u -> u.getId().equals(userId))) {
            board.getLikes().removeIf(u -> u.getId().equals(userId));
        } else {
            board.getLikes().add(user);
        }
    }

    // [규민] 내가 쓴 글 목록
    public List<Board> getMyBoardList(Long userId) {
        return boardRepository.findAllByUserIdOrderByIdDesc(userId);
    }

    // ✅ 5. 게시글 수정
    @Transactional
    public void updateBoard(Long boardId, Long userId, String title, String content) {
        Board board = boardRepository.findByIdWithUserAndLikes(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 없습니다."));

        if (!board.getUser().getId().equals(userId)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        board.setTitle(title);
        board.setContent(content);
    }

    // ✅ 6. 게시글 삭제
    @Transactional
    public void deleteBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 없습니다."));

        if (!board.getUser().getId().equals(userId)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        boardRepository.delete(board);
    }

    // ✅ 7. 최근 게시글 (Lazy 에러 완전 차단)
    public List<Board> getRecentBoards(int limit) {
        return boardRepository.findRecentBoards()
                .stream()
                .limit(limit)
                .toList();
    }
}
