package com.example.etfsj.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BoardListDto {

    private Long id;
    private String title;
    private String username;
    private int views;
    private int likeCount;
    private LocalDateTime createdDate;

    // 🔥 이게 없어서 에러 난 거다
    public static BoardListDto from(Board board) {
        return new BoardListDto(
                board.getId(),
                board.getTitle(),
                board.getUser() != null ? board.getUser().getUsername() : "익명",
                board.getViews(),
                board.getLikes().size(),
                board.getCreatedDate()
        );
    }
}
