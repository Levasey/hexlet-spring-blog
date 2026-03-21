package io.hexletspringblog.controller;

import io.hexletspringblog.dto.PostCreateDTO;
import io.hexletspringblog.dto.PostDTO;
import io.hexletspringblog.dto.PostParamsDTO;
import io.hexletspringblog.dto.PostUpdateDTO;
import io.hexletspringblog.model.User;
import io.hexletspringblog.service.PostService;
import io.hexletspringblog.util.UserUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Posts", description = "Посты блога (чтение публично, изменение — с JWT)")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final UserUtils userUtils;

    // Публичный доступ - разрешен всем
    @SecurityRequirements
    @GetMapping
    public Page<PostDTO> index(
            PostParamsDTO params,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (userUtils.getCurrentUser() == null) {
            params.setPublished(Boolean.TRUE);
        }
        Pageable pageable = PageRequest.of(page, size);
        return postService.findAll(params, pageable);
    }

    // Публичный доступ - разрешен всем
    @SecurityRequirements
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> showPost(@PathVariable Long id) {
        PostDTO postDTO = postService.findById(id);
        return ResponseEntity.ok(postDTO);
    }

    // Требует аутентификации
    @PostMapping
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody PostCreateDTO postCreateDTO) {
        // Можно автоматически устанавливать автора из текущего пользователя
        User currentUser = userUtils.getCurrentUser();
        if (currentUser != null) {
            postCreateDTO.setAuthorId(currentUser.getId());
        }

        PostDTO createdPost = postService.create(postCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    // Требует аутентификации
    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id, @Valid @RequestBody PostUpdateDTO postUpdateDTO) {
        PostDTO updatedPost = postService.update(id, postUpdateDTO);
        return ResponseEntity.ok(updatedPost);
    }

    // Требует аутентификации
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}