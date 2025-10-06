package io.hexletspringblog.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class CommentDTO {
    private Long id;
    private String body;
    private LocalDate createdAt;
    private Long postId;
}
