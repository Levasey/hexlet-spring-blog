package io.hexletspringblog.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {
    private String firstName;
    private String lastName;

    @Email(message = "Email should be valid")
    private String email;
}
