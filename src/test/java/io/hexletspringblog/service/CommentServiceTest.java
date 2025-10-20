package io.hexletspringblog.service;

import io.hexletspringblog.dto.CommentDTO;
import io.hexletspringblog.dto.CommentUpdateDTO;
import io.hexletspringblog.exception.ResourceNotFoundException;
import io.hexletspringblog.mapper.CommentMapper;
import io.hexletspringblog.model.Comment;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.repository.CommentRepository;
import io.hexletspringblog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private Post testPost;
    private Comment testComment;
    private CommentDTO testCommentDTO;

    @BeforeEach
    void setUp() {
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setBody("Test comment body");
        testComment.setPost(testPost);

        testCommentDTO = new CommentDTO();
        testCommentDTO.setId(1L);
        testCommentDTO.setBody("Test comment body");
        testCommentDTO.setPostId(1L);
    }

    @Test
    void findAll_ShouldReturnAllComments() {
        // Arrange
        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setBody("Another comment");
        comment2.setPost(testPost);

        CommentDTO commentDTO2 = new CommentDTO();
        commentDTO2.setId(2L);
        commentDTO2.setBody("Another comment");
        commentDTO2.setPostId(1L);

        when(commentRepository.findAll()).thenReturn(Arrays.asList(testComment, comment2));
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);
        when(commentMapper.toDTO(comment2)).thenReturn(commentDTO2);

        // Act
        List<CommentDTO> result = commentService.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBody()).isEqualTo("Test comment body");
        assertThat(result.get(1).getBody()).isEqualTo("Another comment");
    }

    @Test
    void findById_WhenCommentExists_ShouldReturnComment() {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

        // Act
        CommentDTO result = commentService.findById(1L);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBody()).isEqualTo("Test comment body");
        assertThat(result.getPostId()).isEqualTo(1L);
    }

    @Test
    void findById_WhenCommentNotExists_ShouldThrowException() {
        // Arrange
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commentService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id: 999");
    }

    @Test
    void create_WithValidData_ShouldCreateComment() {
        // Arrange
        CommentDTO newCommentDTO = new CommentDTO();
        newCommentDTO.setBody("New comment");
        newCommentDTO.setPostId(1L);

        Comment newComment = new Comment();
        newComment.setBody("New comment");
        newComment.setPost(testPost);

        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setBody("New comment");
        savedComment.setPost(testPost);

        CommentDTO savedCommentDTO = new CommentDTO();
        savedCommentDTO.setId(1L);
        savedCommentDTO.setBody("New comment");
        savedCommentDTO.setPostId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentMapper.toEntity(newCommentDTO)).thenReturn(newComment);
        when(commentRepository.save(newComment)).thenReturn(savedComment);
        when(commentMapper.toDTO(savedComment)).thenReturn(savedCommentDTO);

        // Act
        CommentDTO result = commentService.create(newCommentDTO);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBody()).isEqualTo("New comment");
        verify(commentRepository).save(newComment);
    }

    @Test
    void create_WithNonExistentPost_ShouldThrowException() {
        // Arrange
        CommentDTO newCommentDTO = new CommentDTO();
        newCommentDTO.setBody("New comment");
        newCommentDTO.setPostId(999L);

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commentService.create(newCommentDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Post not found with id: 999");
    }

    @Test
    void create_WithoutPostId_ShouldThrowException() {
        // Arrange
        CommentDTO newCommentDTO = new CommentDTO();
        newCommentDTO.setBody("New comment");
        // postId is null

        // Act & Assert
        assertThatThrownBy(() -> commentService.create(newCommentDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Post ID is required");
    }

    @Test
    void update_WithValidData_ShouldUpdateComment() {
        // Arrange
        CommentUpdateDTO updateDTO = new CommentUpdateDTO();
        updateDTO.setBody("Updated body");
        updateDTO.setPostId(1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.save(testComment)).thenReturn(testComment);
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

        // Act
        CommentDTO result = commentService.update(1L, updateDTO);

        // Assert
        assertThat(result.getBody()).isEqualTo("Test comment body");
        verify(commentRepository).save(testComment);
    }

    @Test
    void update_WithNewPost_ShouldUpdatePost() {
        // Arrange
        Post newPost = new Post();
        newPost.setId(2L);
        newPost.setTitle("New Post");

        CommentUpdateDTO updateDTO = new CommentUpdateDTO();
        updateDTO.setBody("Updated body");
        updateDTO.setPostId(2L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(postRepository.findById(2L)).thenReturn(Optional.of(newPost));
        when(commentRepository.save(testComment)).thenReturn(testComment);
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

        // Act
        commentService.update(1L, updateDTO);

        // Assert
        assertThat(testComment.getPost()).isEqualTo(newPost);
        verify(commentRepository).save(testComment);
    }

    @Test
    void update_WhenCommentNotExists_ShouldThrowException() {
        // Arrange
        CommentUpdateDTO updateDTO = new CommentUpdateDTO();
        updateDTO.setBody("Updated body");

        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commentService.update(999L, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id: 999");
    }

    @Test
    void delete_WhenCommentExists_ShouldDeleteComment() {
        // Arrange
        when(commentRepository.existsById(1L)).thenReturn(true);

        // Act
        commentService.delete(1L);

        // Assert
        verify(commentRepository).deleteById(1L);
    }

    @Test
    void delete_WhenCommentNotExists_ShouldThrowException() {
        // Arrange
        when(commentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> commentService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id: 999");
    }

    @Test
    void findByPostId_ShouldReturnCommentsForPost() {
        // Arrange
        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setBody("Another comment");
        comment2.setPost(testPost);

        CommentDTO commentDTO2 = new CommentDTO();
        commentDTO2.setId(2L);
        commentDTO2.setBody("Another comment");
        commentDTO2.setPostId(1L);

        when(postRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findAll()).thenReturn(Arrays.asList(testComment, comment2));
        when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);
        when(commentMapper.toDTO(comment2)).thenReturn(commentDTO2);

        // Act
        List<CommentDTO> result = commentService.findByPostId(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(comment -> comment.getPostId().equals(1L));
    }

    @Test
    void findByPostId_WhenPostNotExists_ShouldThrowException() {
        // Arrange
        when(postRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> commentService.findByPostId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Post not found with id: 999");
    }

    @Test
    void existsById_WhenCommentExists_ShouldReturnTrue() {
        // Arrange
        when(commentRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = commentService.existsById(1L);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void existsById_WhenCommentNotExists_ShouldReturnFalse() {
        // Arrange
        when(commentRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = commentService.existsById(999L);

        // Assert
        assertThat(result).isFalse();
    }
}
