package io.hexletspringblog.controller;

import io.hexletspringblog.dto.CommentDTO;
import io.hexletspringblog.exception.ResourceNotFoundException;
import io.hexletspringblog.mapper.CommentMapper;
import io.hexletspringblog.model.Comment;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.repository.CommentRepository;
import io.hexletspringblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDTO create(@RequestBody CommentDTO commentDTO) {
        Post post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        Comment comment = commentMapper.toEntity(commentDTO);
        comment.setPost(post);
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDTO(savedComment);
    }

    @GetMapping(path = "/{id}")
    public CommentDTO show(@PathVariable long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment with id " + id + " not found"));
        return commentMapper.toDTO(comment);
    }

    @PutMapping("/{id}")
    public CommentDTO update(@PathVariable long id, @RequestBody CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment with id " + id + " not found"));

        comment.setBody(commentDTO.getBody());

        // Если изменился postId, обновляем связь с постом
        if (commentDTO.getPostId() != null &&
                (comment.getPost() == null || !commentDTO.getPostId().equals(comment.getPost().getId()))) {
            var post = postRepository.findById(commentDTO.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + commentDTO.getPostId()));
            comment.setPost(post);
        }

        Comment saved = commentRepository.save(comment);

        return commentMapper.toDTO(saved);
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
