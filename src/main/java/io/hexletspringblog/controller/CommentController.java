package io.hexletspringblog.controller;

import io.hexletspringblog.dto.CommentDTO;
import io.hexletspringblog.dto.CommentUpdateDTO;
import io.hexletspringblog.exception.ResourceNotFoundException;
import io.hexletspringblog.mapper.CommentMapper;
import io.hexletspringblog.model.Comment;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.repository.CommentRepository;
import io.hexletspringblog.repository.PostRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentMapper commentMapper;

    @GetMapping(path = "")
    public List<CommentDTO> index() {
        return commentRepository.findAll().stream().map(commentMapper::toDTO).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<CommentDTO> create(@Valid @RequestBody CommentDTO commentDTO) {
        // Проверяем существование поста
        if (commentDTO.getPostId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID is required");
        }

        Post post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + commentDTO.getPostId())); // Используем ResourceNotFoundException

        Comment comment = commentMapper.toEntity(commentDTO);
        comment.setPost(post); // Устанавливаем связь с постом

        Comment saved = commentRepository.save(comment);
        CommentDTO savedDTO = commentMapper.toDTO(saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    @GetMapping(path = "/{id}")
    public CommentDTO show(@PathVariable long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment with id " + id + " not found"));
        return commentMapper.toDTO(comment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> update(@PathVariable long id, @Valid @RequestBody CommentUpdateDTO commentUpdateDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        // Если меняется пост, проверяем его существование
        if (commentUpdateDTO.getPostId() != null && !commentUpdateDTO.getPostId().equals(comment.getPost().getId())) {
            Post post = postRepository.findById(commentUpdateDTO.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + commentUpdateDTO.getPostId()));
            comment.setPost(post);
        }

        comment.setBody(commentUpdateDTO.getBody());

        Comment updated = commentRepository.save(comment);
        CommentDTO updatedDTO = commentMapper.toDTO(updated);

        return ResponseEntity.ok(updatedDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable long id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment with id " + id + " not found");
        }
        commentRepository.deleteById(id);
    }
}
