package io.hexletspringblog.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Setter
@Getter
public class CommentUpdateDTO {
    @NotBlank(message = "Body must not be blank")
    private String body;

    // postId не обязателен для обновления
    private Long postId;
}
