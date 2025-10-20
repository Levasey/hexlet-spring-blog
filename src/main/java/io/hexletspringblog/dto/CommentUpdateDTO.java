package io.hexletspringblog.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Setter
@Getter
public class CommentUpdateDTO {
    @NotBlank(message = "Body is required")
    @Size(min = 1, max = 1000, message = "Body must be between 1 and 1000 characters")
    private String body;

    private Long postId;
}
