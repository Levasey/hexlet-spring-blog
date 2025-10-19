package io.hexletspringblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagCreateDTO {
    @NotBlank(message = "Tag name cannot be blank")
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    private String name;
}
