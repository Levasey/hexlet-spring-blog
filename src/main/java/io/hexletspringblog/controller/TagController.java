package io.hexletspringblog.controller;

import io.hexletspringblog.dto.TagCreateDTO;
import io.hexletspringblog.dto.TagDTO;
import io.hexletspringblog.dto.TagUpdateDTO;
import io.hexletspringblog.service.TagService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tags", description = "Теги постов")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // Публичный доступ
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagService.findAll();
        return ResponseEntity.ok(tags);
    }

    // Публичный доступ
    @SecurityRequirements
    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long id) {
        TagDTO tag = tagService.findById(id);
        return ResponseEntity.ok(tag);
    }

    // Требует аутентификации
    @PostMapping
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagCreateDTO tagCreateDTO) {
        TagDTO createdTag = tagService.create(tagCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    // Требует аутентификации
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
