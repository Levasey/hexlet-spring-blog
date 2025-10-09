package io.hexletspringblog.controller;

import io.hexletspringblog.dto.UserCreateDTO;
import io.hexletspringblog.dto.UserDTO;
import io.hexletspringblog.exception.ResourceAlreadyExistsException;
import io.hexletspringblog.exception.ResourceNotFoundException;
import io.hexletspringblog.mapper.UserMapper;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

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
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserCreateDTO userCreateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        // Update only the fields that are provided in the request
        if (userCreateDTO.getFirstName() != null) {
            user.setFirstName(userCreateDTO.getFirstName());
        }
        if (userCreateDTO.getLastName() != null) {
            user.setLastName(userCreateDTO.getLastName());
        }
        if (userCreateDTO.getEmail() != null) {
            user.setEmail(userCreateDTO.getEmail());
        }

        User updatedUser = userRepository.save(user);
        UserDTO userDTO = userMapper.toUserDTO(updatedUser);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<User> showUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
