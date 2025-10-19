package io.hexletspringblog.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TagUpdateDTO {
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    private JsonNullable<String> name = JsonNullable.undefined();
}
