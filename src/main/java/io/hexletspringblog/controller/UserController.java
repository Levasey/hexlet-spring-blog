package io.hexletspringblog.controller;

import io.hexletspringblog.dto.UserCreateDTO;
import io.hexletspringblog.dto.UserDTO;
import io.hexletspringblog.dto.UserRegistrationDTO;
import io.hexletspringblog.dto.UserUpdateDTO;
import io.hexletspringblog.exception.ResourceAlreadyExistsException;
import io.hexletspringblog.exception.ResourceNotFoundException;
import io.hexletspringblog.mapper.UserMapper;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.UserRepository;
import io.hexletspringblog.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CustomUserDetailsService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserDTO).toList();
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        if (userRepository.findByEmail(userCreateDTO.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with this email already exists");
        }
        User user = userMapper.toEntity(userCreateDTO);
        User saved = userRepository.save(user);
        UserDTO userDTO = userMapper.toUserDTO(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        if (userUpdateDTO.getEmail() != null &&
                !userUpdateDTO.getEmail().equals(user.getEmail()) &&
                userRepository.findByEmail(userUpdateDTO.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with this email already exists");
        }

        userMapper.updateEntityFromDTO(userUpdateDTO, user);

        userRepository.save(user);

        return ResponseEntity.ok(userMapper.toUserDTO(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> showUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(userMapper.toUserDTO(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        if (userService.userExists(registrationDTO.getEmail())) {
            return ResponseEntity.badRequest().body("User already exists");
        }

        User user = new User();
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setEmail(registrationDTO.getEmail());
        user.setPasswordDigest(registrationDTO.getPassword()); // Будет закодирован в сервисе

        userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }
}
