package io.hexletspringblog.service;

import io.hexletspringblog.dto.PostCreateDTO;
import io.hexletspringblog.dto.PostDTO;
import io.hexletspringblog.dto.PostParamsDTO;
import io.hexletspringblog.dto.PostUpdateDTO;
import io.hexletspringblog.exception.ResourceNotFoundException;
import io.hexletspringblog.mapper.PostMapper;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.Tag;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.PostRepository;
import io.hexletspringblog.repository.TagRepository;
import io.hexletspringblog.repository.UserRepository;
import io.hexletspringblog.specification.PostSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostSpecification postSpecification;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Post testPost;
    private PostDTO testPostDTO;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        testUser.setPasswordDigest("validPassword123");

        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Java");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setSlug("test-post");
        testPost.setPublished(true);
        testPost.setAuthor(testUser);
        testPost.setTags(List.of(testTag));
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        testPostDTO = new PostDTO();
        testPostDTO.setId(1L);
        testPostDTO.setTitle("Test Post");
        testPostDTO.setContent("Test Content");
        testPostDTO.setSlug("test-post");
        testPostDTO.setPublished(true);
        testPostDTO.setAuthorId(1L);
        testPostDTO.setCreatedAt(LocalDateTime.now());
        testPostDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void findAll_WithParams_ShouldReturnPageOfPosts() {
        // Arrange
        PostParamsDTO params = new PostParamsDTO();
        params.setNameCont("test");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(testPost), pageable, 1);

        when(postSpecification.build(any(PostParamsDTO.class))).thenReturn(mock(Specification.class));
        when(postRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(postPage);
        when(postMapper.toDTO(testPost)).thenReturn(testPostDTO);

        // Act
        Page<PostDTO> result = postService.findAll(params, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Post");
        verify(postSpecification).build(params);
        verify(postRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAll_WithoutParams_ShouldReturnAllPosts() {
        // Arrange
        PostParamsDTO params = new PostParamsDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(testPost), pageable, 1);

        when(postSpecification.build(any(PostParamsDTO.class))).thenReturn(mock(Specification.class));
        when(postRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(postPage);
        when(postMapper.toDTO(testPost)).thenReturn(testPostDTO);

        // Act
        Page<PostDTO> result = postService.findAll(params, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(postRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findById_WhenPostExists_ShouldReturnPost() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postMapper.toDTO(testPost)).thenReturn(testPostDTO);

        // Act
        PostDTO result = postService.findById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Post");
        verify(postRepository).findById(1L);
    }

    @Test
    void findById_WhenPostNotExists_ShouldThrowException() {
        // Arrange
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> postService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Post not found with id: 999");

        verify(postRepository).findById(999L);
    }

    @Test
    void create_WithValidData_ShouldCreatePost() {
        // Arrange
        PostCreateDTO createDTO = new PostCreateDTO();
        createDTO.setTitle("New Post");
        createDTO.setContent("New Content");
        createDTO.setSlug("new-post");
        createDTO.setPublished(true);
        createDTO.setAuthorId(1L);
        createDTO.setTagIds(List.of(1L));

        Post newPost = new Post();
        newPost.setTitle("New Post");
        newPost.setContent("New Content");
        newPost.setSlug("new-post");
        newPost.setPublished(true);
        newPost.setAuthor(testUser);

        Post savedPost = new Post();
        savedPost.setId(2L);
        savedPost.setTitle("New Post");
        savedPost.setContent("New Content");
        savedPost.setSlug("new-post");
        savedPost.setPublished(true);
        savedPost.setAuthor(testUser);
        savedPost.setTags(List.of(testTag));

        PostDTO savedPostDTO = new PostDTO();
        savedPostDTO.setId(2L);
        savedPostDTO.setTitle("New Post");
        savedPostDTO.setContent("New Content");
        savedPostDTO.setSlug("new-post");
        savedPostDTO.setPublished(true);
        savedPostDTO.setAuthorId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(postMapper.toEntity(createDTO)).thenReturn(newPost);
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(testTag));
        when(postRepository.save(newPost)).thenReturn(savedPost);
        when(postMapper.toDTO(savedPost)).thenReturn(savedPostDTO);

        // Act
        PostDTO result = postService.create(createDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New Post");
        verify(userRepository).findById(1L);
        verify(tagRepository).findAllById(List.of(1L));
        verify(postRepository).save(newPost);
    }

    @Test
    void create_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        PostCreateDTO createDTO = new PostCreateDTO();
        createDTO.setAuthorId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> postService.create(createDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(postRepository, never()).save(any());
    }

    @Test
    void create_WithoutTags_ShouldCreatePostWithoutTags() {
        // Arrange
        PostCreateDTO createDTO = new PostCreateDTO();
        createDTO.setTitle("New Post");
        createDTO.setContent("New Content");
        createDTO.setSlug("new-post");
        createDTO.setPublished(true);
        createDTO.setAuthorId(1L);
        // No tags

        Post newPost = new Post();
        newPost.setTitle("New Post");
        newPost.setContent("New Content");
        newPost.setSlug("new-post");
        newPost.setPublished(true);
        newPost.setAuthor(testUser);

        Post savedPost = new Post();
        savedPost.setId(2L);
        savedPost.setTitle("New Post");
        savedPost.setContent("New Content");
        savedPost.setSlug("new-post");
        savedPost.setPublished(true);
        savedPost.setAuthor(testUser);

        PostDTO savedPostDTO = new PostDTO();
        savedPostDTO.setId(2L);
        savedPostDTO.setTitle("New Post");
        savedPostDTO.setContent("New Content");
        savedPostDTO.setSlug("new-post");
        savedPostDTO.setPublished(true);
        savedPostDTO.setAuthorId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(postMapper.toEntity(createDTO)).thenReturn(newPost);
        when(postRepository.save(newPost)).thenReturn(savedPost);
        when(postMapper.toDTO(savedPost)).thenReturn(savedPostDTO);

        // Act
        PostDTO result = postService.create(createDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(tagRepository, never()).findAllById(any());
        verify(postRepository).save(newPost);
    }

    @Test
    void update_WithValidData_ShouldUpdatePost() {
        // Arrange
        PostUpdateDTO updateDTO = new PostUpdateDTO();
        updateDTO.setTitle(org.openapitools.jackson.nullable.JsonNullable.of("Updated Title"));
        updateDTO.setContent(org.openapitools.jackson.nullable.JsonNullable.of("Updated Content"));

        Post updatedPost = new Post();
        updatedPost.setId(1L);
        updatedPost.setTitle("Updated Title");
        updatedPost.setContent("Updated Content");
        updatedPost.setSlug("test-post");
        updatedPost.setPublished(true);
        updatedPost.setAuthor(testUser);

        PostDTO updatedPostDTO = new PostDTO();
        updatedPostDTO.setId(1L);
        updatedPostDTO.setTitle("Updated Title");
        updatedPostDTO.setContent("Updated Content");
        updatedPostDTO.setSlug("test-post");
        updatedPostDTO.setPublished(true);
        updatedPostDTO.setAuthorId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        doNothing().when(postMapper).updateEntityFromDTO(updateDTO, testPost);
        when(postRepository.save(testPost)).thenReturn(updatedPost);
        when(postMapper.toDTO(updatedPost)).thenReturn(updatedPostDTO);

        // Act
        PostDTO result = postService.update(1L, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getContent()).isEqualTo("Updated Content");
        verify(postMapper).updateEntityFromDTO(updateDTO, testPost);
        verify(postRepository).save(testPost);
    }

    @Test
    void update_WithTags_ShouldUpdatePostTags() {
        // Arrange
        Tag newTag = new Tag();
        newTag.setId(2L);
        newTag.setName("Spring");

        PostUpdateDTO updateDTO = new PostUpdateDTO();
        updateDTO.setTagIds(org.openapitools.jackson.nullable.JsonNullable.of(List.of(2L)));

        Post updatedPost = new Post();
        updatedPost.setId(1L);
        updatedPost.setTitle("Test Post");
        updatedPost.setContent("Test Content");
        updatedPost.setSlug("test-post");
        updatedPost.setPublished(true);
        updatedPost.setAuthor(testUser);
        updatedPost.setTags(List.of(newTag));

        PostDTO updatedPostDTO = new PostDTO();
        updatedPostDTO.setId(1L);
        updatedPostDTO.setTitle("Test Post");
        updatedPostDTO.setContent("Test Content");
        updatedPostDTO.setSlug("test-post");
        updatedPostDTO.setPublished(true);
        updatedPostDTO.setAuthorId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        doNothing().when(postMapper).updateEntityFromDTO(updateDTO, testPost);
        when(tagRepository.findAllById(List.of(2L))).thenReturn(List.of(newTag));
        when(postRepository.save(testPost)).thenReturn(updatedPost);
        when(postMapper.toDTO(updatedPost)).thenReturn(updatedPostDTO);

        // Act
        PostDTO result = postService.update(1L, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(tagRepository).findAllById(List.of(2L));
        assertThat(testPost.getTags()).contains(newTag);
    }

    @Test
    void update_WithNonExistentPost_ShouldThrowException() {
        // Arrange
        PostUpdateDTO updateDTO = new PostUpdateDTO();
        updateDTO.setTitle(org.openapitools.jackson.nullable.JsonNullable.of("Updated Title"));

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> postService.update(999L, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Post not found with id: 999");

        verify(postRepository).findById(999L);
        verify(postRepository, never()).save(any());
    }

    @Test
    void update_WithNonExistentTags_ShouldUpdatePostWithoutTags() {
        // Arrange
        PostUpdateDTO updateDTO = new PostUpdateDTO();
        updateDTO.setTagIds(org.openapitools.jackson.nullable.JsonNullable.of(List.of(999L)));

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        doNothing().when(postMapper).updateEntityFromDTO(updateDTO, testPost);
        when(tagRepository.findAllById(List.of(999L))).thenReturn(List.of());
        when(postRepository.save(testPost)).thenReturn(testPost);
        when(postMapper.toDTO(testPost)).thenReturn(testPostDTO);

        // Act
        PostDTO result = postService.update(1L, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(tagRepository).findAllById(List.of(999L));
        assertThat(testPost.getTags()).isEmpty();
    }

    @Test
    void delete_WhenPostExists_ShouldDeletePost() {
        // Arrange
        when(postRepository.existsById(1L)).thenReturn(true);

        // Act
        postService.delete(1L);

        // Assert
        verify(postRepository).existsById(1L);
        verify(postRepository).deleteById(1L);
    }

    @Test
    void delete_WhenPostNotExists_ShouldThrowException() {
        // Arrange
        when(postRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> postService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Post not found with id: 999");

        verify(postRepository).existsById(999L);
        verify(postRepository, never()).deleteById(anyLong());
    }

    @Test
    void findByTagId_WhenTagExists_ShouldReturnPosts() {
        // Arrange
        when(tagRepository.existsById(1L)).thenReturn(true);
        when(postRepository.findByTagId(1L)).thenReturn(List.of(testPost));
        when(postMapper.toDTO(testPost)).thenReturn(testPostDTO);

        // Act
        List<PostDTO> result = postService.findByTagId(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Post");
        verify(tagRepository).existsById(1L);
        verify(postRepository).findByTagId(1L);
    }

    @Test
    void findByTagId_WhenTagNotExists_ShouldThrowException() {
        // Arrange
        when(tagRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> postService.findByTagId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tag not found with id: 999");

        verify(tagRepository).existsById(999L);
        verify(postRepository, never()).findByTagId(anyLong());
    }

    @Test
    void findByTagIds_ShouldReturnPostsWithGivenTags() {
        // Arrange
        List<Long> tagIds = List.of(1L, 2L);
        when(postRepository.findByTagIds(tagIds)).thenReturn(List.of(testPost));
        when(postMapper.toDTO(testPost)).thenReturn(testPostDTO);

        // Act
        List<PostDTO> result = postService.findByTagIds(tagIds);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Post");
        verify(postRepository).findByTagIds(tagIds);
    }

    @Test
    void findByTagIds_WithEmptyList_ShouldReturnEmptyList() {
        // Arrange
        List<Long> tagIds = List.of();
        when(postRepository.findByTagIds(tagIds)).thenReturn(List.of());

        // Act
        List<PostDTO> result = postService.findByTagIds(tagIds);

        // Assert
        assertThat(result).isEmpty();
        verify(postRepository).findByTagIds(tagIds);
    }
}