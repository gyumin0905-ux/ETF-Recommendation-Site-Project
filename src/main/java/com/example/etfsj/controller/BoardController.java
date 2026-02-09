package com.example.etfsj.controller;

import com.example.etfsj.domain.Board;
import com.example.etfsj.domain.BoardListDto;
import com.example.etfsj.domain.Comment;
import com.example.etfsj.domain.User;
import com.example.etfsj.service.BoardService;
import com.example.etfsj.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    // ✅ 1. 게시판 목록 페이지 (DTO)
    @GetMapping
    public String boardList(Model model) {
        List<BoardListDto> boards = boardService.getBoardList();
        model.addAttribute("boards", boards);
        return "board/list";
    }

    // 2. 글쓰기 페이지 이동
    @GetMapping("/write")
    public String writeForm(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }
        return "board/form";
    }

    // 3. 글 저장
    @PostMapping("/save")
    public String saveBoard(@RequestParam("title") String title,
                            @RequestParam("content") String content,
                            HttpSession session) {

        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        boardService.writeBoard(title, content, user.getId());
        return "redirect:/board";
    }

    // ✅ 4. 글 상세 보기
    // (게시글 + 좋아요 여부 + 작성자 여부 + 댓글 + 최근 게시글)
    @GetMapping("/{id}")
    public String boardDetail(@PathVariable Long id,
                              HttpSession session,
                              Model model) {

        // 게시글
        Board board = boardService.getBoard(id);

        // 로그인 유저
        User loginUser = (User) session.getAttribute("loginUser");

        Long loginUserId = null;

        // 좋아요 여부
        boolean isLiked = false;
        if (loginUser != null) {
            isLiked = board.getLikes().stream()
                    .anyMatch(u -> u.getId().equals(loginUser.getId()));
            loginUserId = loginUser.getId();
        }

        // 작성자 여부
        boolean isOwner = false;
        if (loginUser != null && board.getUser() != null) {
            isOwner = board.getUser().getId().equals(loginUser.getId());
        }

        // 댓글 목록
        List<Comment> comments = commentService.getComments(id);

        // 최근 게시글 5개
        List<Board> recentBoards = boardService.getRecentBoards(5);

        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("board", board);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("comments", comments);
        model.addAttribute("recentBoards", recentBoards);

        return "board/detail";
    }

    // 5. 좋아요 토글
    @PostMapping("/{id}/like")
    public String likeBoard(@PathVariable Long id,
                            HttpSession session) {

        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        boardService.toggleLike(id, user.getId());
        return "redirect:/board/" + id;
    }

    // ✅ 6. 게시글 수정 페이지 이동 (작성자만)
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           HttpSession session,
                           Model model) {

        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        Board board = boardService.getBoard(id);

        // 작성자 본인 체크
        if (board.getUser() == null || !board.getUser().getId().equals(user.getId())) {
            return "redirect:/board/" + id;
        }

        model.addAttribute("loginUserId", user.getId());
        model.addAttribute("board", board);
        return "board/edit";
    }

    // ✅ 7. 게시글 수정 처리 (작성자만)
    @PostMapping("/{id}/update")
    public String updateBoard(@PathVariable Long id,
                              @RequestParam("title") String title,
                              @RequestParam("content") String content,
                              HttpSession session) {

        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        boardService.updateBoard(id, user.getId(), title, content);
        return "redirect:/board/" + id;
    }

    // ✅ 8. 게시글 삭제 처리 (작성자만)
    @PostMapping("/{id}/delete")
    public String deleteBoard(@PathVariable Long id,
                              HttpSession session) {

        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        boardService.deleteBoard(id, user.getId());
        return "redirect:/board";
    }
}
