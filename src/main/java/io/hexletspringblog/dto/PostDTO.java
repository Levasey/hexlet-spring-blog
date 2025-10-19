package io.hexletspringblog.dto;

import io.hexletspringblog.model.Tag;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PostDTO {
    private Long id;
    private Long authorId;
    private List<Tag> tags = new ArrayList<>();
    private List<CommentDTO> comments = new ArrayList<>();
    private String slug;
    private String title;
    private String content;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
