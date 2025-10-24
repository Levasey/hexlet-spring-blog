package io.hexletspringblog.controller;

import io.hexletspringblog.dto.CommentDTO;
import io.hexletspringblog.dto.CommentUpdateDTO;
import io.hexletspringblog.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping(path = "")
    public List<CommentDTO> index() {
        return commentService.findAll();
    }

    @PostMapping
    public ResponseEntity<CommentDTO> create(@Valid @RequestBody CommentDTO commentDTO) {
        CommentDTO savedDTO = commentService.create(commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    @GetMapping(path = "/{id}")
    public CommentDTO show(@PathVariable long id) {
        return commentService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> update(@PathVariable long id, @Valid @RequestBody CommentUpdateDTO commentUpdateDTO) {
        CommentDTO updatedDTO = commentService.update(id, commentUpdateDTO);
        return ResponseEntity.ok(updatedDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable long id) {
        commentService.delete(id);
    }

    @GetMapping("/post/{postId}")
    public List<CommentDTO> getCommentsByPost(@PathVariable Long postId) {
        return commentService.findByPostId(postId);
    }
}
