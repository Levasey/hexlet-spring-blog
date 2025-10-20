package io.hexletspringblog.service;

import io.hexletspringblog.dto.CommentDTO;
import io.hexletspringblog.dto.CommentUpdateDTO;
import io.hexletspringblog.exception.ResourceNotFoundException;
import io.hexletspringblog.mapper.CommentMapper;
import io.hexletspringblog.model.Comment;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.repository.CommentRepository;
import io.hexletspringblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.commentMapper = commentMapper;
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findAll() {
        return commentRepository.findAll()
                .stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommentDTO findById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
        return commentMapper.toDTO(comment);
    }

    public CommentDTO create(CommentDTO commentDTO) {
        if (commentDTO.getPostId() == null) {
            throw new IllegalArgumentException("Post ID is required");
        }

        Post post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + commentDTO.getPostId()));

        Comment comment = commentMapper.toEntity(commentDTO);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDTO(savedComment);
    }

    public CommentDTO update(Long id, CommentUpdateDTO commentUpdateDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        // Если меняется пост, проверяем его существование
        if (commentUpdateDTO.getPostId() != null && !commentUpdateDTO.getPostId().equals(comment.getPost().getId())) {
            Post post = postRepository.findById(commentUpdateDTO.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + commentUpdateDTO.getPostId()));
            comment.setPost(post);
        }

        comment.setBody(commentUpdateDTO.getBody());

        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toDTO(updatedComment);
    }

    public void delete(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment not found with id: " + id);
        }
        commentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        return commentRepository.findAll().stream()
                .filter(comment -> comment.getPost().getId().equals(postId))
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return commentRepository.existsById(id);
    }
}