package io.hexletspringblog.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class CommentUpdateDTO {
    private JsonNullable<String> body;
    private JsonNullable<Long> postId;
}
