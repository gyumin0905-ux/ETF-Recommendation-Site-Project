package com.example.etfsj.controller;

import com.example.etfsj.domain.User;
import com.example.etfsj.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/board/{boardId}/comment")
    public String writeComment(@PathVariable Long boardId,
                               @RequestParam String content,
                               HttpSession session) {

        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        commentService.writeComment(boardId, user.getId(), content);
        return "redirect:/board/" + boardId;
    }

    // 댓글 삭제 (본인만)
    @PostMapping("/comment/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                HttpSession session) {

        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        commentService.deleteComment(id, user.getId());

        return "redirect:/board";
    }
}
