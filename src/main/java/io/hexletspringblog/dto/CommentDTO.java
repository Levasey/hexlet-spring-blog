package io.hexletspringblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class CommentDTO {
    private Long id;

    @NotBlank(message = "Body must not be blank")
    private String body;

    private LocalDate createdAt;

    @NotNull(message = "Post ID is required")
    private Long postId;
}
