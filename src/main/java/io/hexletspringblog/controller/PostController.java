package io.hexletspringblog.controller;

import io.hexletspringblog.dto.PostDTO;
import io.hexletspringblog.exception.ResourceNotFoundException;
import io.hexletspringblog.mapper.PostMapper;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.repository.CommentRepository;
import io.hexletspringblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping
    public Page<PostDTO> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAll(pageable)
                .map(post -> postMapper.toDTO(post));
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody Post post) {
        Post saved = postRepository.save(post);
        PostDTO postDTO = postMapper.toDTO(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(postDTO);
    }

    @GetMapping("/{id}") // Вывод поста
    public ResponseEntity<PostDTO> showPost(@PathVariable Long id) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        PostDTO postDTO = postMapper.toDTO(post);
        return ResponseEntity.ok(postDTO);
    }

    @PutMapping("/{id}") // Обновление поста
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id, @RequestBody Post data) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        if (data.getTitle() != null) {
            post.setTitle(data.getTitle());
        }
        if (data.getContent() != null) {
            post.setContent(data.getContent());
        }
        if (data.getAuthor() != null) {
            post.setAuthor(data.getAuthor());
        }
        Post saved = postRepository.save(post);
        PostDTO postDTO = postMapper.toDTO(saved);
        return ResponseEntity.status(HttpStatus.OK).body(postDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found with id: " + id);
        }
        commentRepository.deleteByPostId(id);
        postRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
