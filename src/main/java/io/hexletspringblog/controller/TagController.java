package io.hexletspringblog.controller;

import io.hexletspringblog.dto.TagCreateDTO;
import io.hexletspringblog.dto.TagDTO;
import io.hexletspringblog.dto.TagUpdateDTO;
import io.hexletspringblog.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagService.findAll();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long id) {
        TagDTO tag = tagService.findById(id);
        return ResponseEntity.ok(tag);
    }

    @PostMapping
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagCreateDTO tagCreateDTO) {
        TagDTO createdTag = tagService.create(tagCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<TagDTO>> createTags(@Valid @RequestBody List<TagCreateDTO> tagCreateDTOs) {
        List<TagDTO> createdTags = tagService.createBulk(tagCreateDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTags);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagUpdateDTO tagUpdateDTO) {
        TagDTO updatedTag = tagService.update(id, tagUpdateDTO);
        return ResponseEntity.ok(updatedTag);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
