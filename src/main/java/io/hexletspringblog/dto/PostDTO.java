package io.hexletspringblog.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private String author;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private List<CommentDTO> comments = new ArrayList<>();
}
