package io.hexletspringblog.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PostDTO {
    private Long id;
    private Long authorId;
    private String slug;
    private String name;
    private String body;
    private LocalDate createdAt;
}
