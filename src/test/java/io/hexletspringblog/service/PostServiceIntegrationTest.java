package io.hexletspringblog.service;

import io.hexletspringblog.dto.PostCreateDTO;
import io.hexletspringblog.dto.PostDTO;
import io.hexletspringblog.dto.PostParamsDTO;
import io.hexletspringblog.dto.PostUpdateDTO;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.Tag;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.PostRepository;
import io.hexletspringblog.repository.TagRepository;
import io.hexletspringblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private User testUser;
    private Tag testTag;
    private Post testPost;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        testUser.setPasswordDigest("validPassword123");
        testUser = userRepository.save(testUser);

        testTag = new Tag();
        testTag.setName("Java");
        testTag = tagRepository.save(testTag);

        testPost = new Post();
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setSlug("test-post");
        testPost.setPublished(true);
        testPost.setAuthor(testUser);
        testPost.getTags().add(testTag); // Используем прямой доступ к коллекции
        testPost = postRepository.save(testPost);
    }

    @Test
    void findAll_ShouldReturnAllPosts() {
        // Arrange
        PostParamsDTO params = new PostParamsDTO();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<PostDTO> result = postService.findAll(params, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Post");
    }

    @Test
    void findById_WhenPostExists_ShouldReturnPost() {
        // Act
        PostDTO result = postService.findById(testPost.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testPost.getId());
        assertThat(result.getTitle()).isEqualTo("Test Post");
        assertThat(result.getAuthorId()).isEqualTo(testUser.getId());
    }

    @Test
    void findById_WhenPostNotExists_ShouldThrowException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> postService.findById(999L));
    }

    @Test
    void create_WithValidData_ShouldCreatePost() {
        // Arrange
        PostCreateDTO createDTO = new PostCreateDTO();
        createDTO.setTitle("New Post");
        createDTO.setContent("New Content");
        createDTO.setSlug("new-post");
        createDTO.setPublished(false);
        createDTO.setAuthorId(testUser.getId());
        createDTO.setTagIds(List.of(testTag.getId()));

        // Act
        PostDTO result = postService.create(createDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Post");
        assertThat(result.getContent()).isEqualTo("New Content");
        assertThat(result.getSlug()).isEqualTo("new-post");
        assertThat(result.isPublished()).isFalse();
        assertThat(result.getAuthorId()).isEqualTo(testUser.getId());

        // Verify in database
        Optional<Post> savedPost = postRepository.findById(result.getId());
        assertThat(savedPost).isPresent();
        assertThat(savedPost.get().getTitle()).isEqualTo("New Post");
        assertThat(savedPost.get().getTags()).hasSize(1);
    }

    @Test
    void update_WithValidData_ShouldUpdatePost() {
        // Arrange
        PostUpdateDTO updateDTO = new PostUpdateDTO();
        updateDTO.setTitle(org.openapitools.jackson.nullable.JsonNullable.of("Updated Title"));
        updateDTO.setContent(org.openapitools.jackson.nullable.JsonNullable.of("Updated Content"));
        updateDTO.setPublished(org.openapitools.jackson.nullable.JsonNullable.of(false));

        // Act
        PostDTO result = postService.update(testPost.getId(), updateDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getContent()).isEqualTo("Updated Content");
        assertThat(result.isPublished()).isFalse();

        // Verify in database
        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedPost.getContent()).isEqualTo("Updated Content");
        assertThat(updatedPost.isPublished()).isFalse();
    }

    @Test
    void update_WithTags_ShouldUpdatePostTags() {
        // Arrange
        Tag newTag = new Tag();
        newTag.setName("Spring");
        newTag = tagRepository.save(newTag);

        PostUpdateDTO updateDTO = new PostUpdateDTO();
        updateDTO.setTagIds(org.openapitools.jackson.nullable.JsonNullable.of(List.of(newTag.getId())));

        // Act
        PostDTO result = postService.update(testPost.getId(), updateDTO);

        // Assert
        assertThat(result).isNotNull();

        // Verify in database
        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(updatedPost.getTags()).hasSize(1);
        assertThat(updatedPost.getTags().get(0).getName()).isEqualTo("Spring");
    }

    @Test
    void delete_WhenPostExists_ShouldDeletePost() {
        // Act
        postService.delete(testPost.getId());

        // Assert
        Optional<Post> deletedPost = postRepository.findById(testPost.getId());
        assertThat(deletedPost).isEmpty();
    }

    @Test
    void findByTagId_ShouldReturnPostsWithTag() {
        // Act
        List<PostDTO> result = postService.findByTagId(testTag.getId());

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testPost.getId());
    }

    @Test
    void findByTagIds_ShouldReturnPostsWithGivenTags() {
        // Act
        List<PostDTO> result = postService.findByTagIds(List.of(testTag.getId()));

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testPost.getId());
    }

    @Test
    void create_WithoutTags_ShouldCreatePostWithoutTags() {
        // Arrange
        PostCreateDTO createDTO = new PostCreateDTO();
        createDTO.setTitle("Post Without Tags");
        createDTO.setContent("Content without tags");
        createDTO.setSlug("post-without-tags");
        createDTO.setPublished(true);
        createDTO.setAuthorId(testUser.getId());
        // No tags specified

        // Act
        PostDTO result = postService.create(createDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Post Without Tags");

        // Verify in database
        Post savedPost = postRepository.findById(result.getId()).orElseThrow();
        assertThat(savedPost.getTags()).isEmpty();
    }
}