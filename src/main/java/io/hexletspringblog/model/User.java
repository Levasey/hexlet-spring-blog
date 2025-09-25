package io.hexletspringblog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "FirstName cannot be blank")
    @Size(min = 2, max = 30, message = "FirstName must be between 2 and 30 characters")
    private String firstName;

    @NotBlank(message = "LastName cannot be blank")
    @Size(min = 2, max = 30, message = "LastName must be between 2 and 30 characters")
    private String lastName;

    @Column(nullable = false)
    @Email
    private String email;

    private LocalDate birthday;
}
