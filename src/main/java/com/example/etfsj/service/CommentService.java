package com.example.etfsj.service;

import com.example.etfsj.domain.Board;
import com.example.etfsj.domain.Comment;
import com.example.etfsj.domain.User;
import com.example.etfsj.repository.BoardRepository;
import com.example.etfsj.repository.CommentRepository;
import com.example.etfsj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 댓글 목록
    public List<Comment> getComments(Long boardId) {
        return commentRepository.findByBoardIdWithUser(boardId);
    }

    // 댓글 작성
    @Transactional
    public void writeComment(Long boardId, Long userId, String content) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Comment comment = new Comment();
        comment.setBoard(board);
        comment.setUser(user);
        comment.setContent(content);

        commentRepository.save(comment);
    }

    // 댓글 삭제 (본인만)
    @Transactional
    public void deleteComment(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("삭제 권한 없음");
        }

        commentRepository.delete(comment);
    }
}
