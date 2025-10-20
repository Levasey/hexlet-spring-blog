package io.hexletspringblog.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

@Getter
@Setter
public class PostUpdateDTO {

    private JsonNullable<List<Long>> tagIds = JsonNullable.undefined();

    private JsonNullable<String> slug = JsonNullable.undefined();

    private JsonNullable<Long> authorId = JsonNullable.undefined();

    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private JsonNullable<String> title;

    @Size(min = 10, message = "Content must be at least 10 characters long")
    private JsonNullable<String> content;

    private JsonNullable<Boolean> published = JsonNullable.undefined();
}
