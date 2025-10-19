package io.hexletspringblog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class PostUpdateDTO {

    @NotNull
    private JsonNullable<Long> authorId;

    @NotNull
    private JsonNullable<String> slug;

    @NotNull
    private JsonNullable<String> title;

    @NotNull
    private JsonNullable<String> content;
}
