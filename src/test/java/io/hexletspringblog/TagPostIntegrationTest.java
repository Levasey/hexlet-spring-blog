package io.hexletspringblog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexletspringblog.dto.*;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.Tag;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.PostRepository;
import io.hexletspringblog.repository.TagRepository;
import io.hexletspringblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TagPostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Tag javaTag;
    private Tag springTag;
    private Tag hibernateTag;
    private Post postWithTags;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();

        objectMapper.registerModule(new JsonNullableModule());

        // Create test user
        testUser = new User();
        testUser.setFirstName("testFirstName");
        testUser.setLastName("testLastName");
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);

        // Create test tags
        javaTag = new Tag();
        javaTag.setName("java");
        javaTag = tagRepository.save(javaTag);

        springTag = new Tag();
        springTag.setName("spring");
        springTag = tagRepository.save(springTag);

        hibernateTag = new Tag();
        hibernateTag.setName("hibernate");
        hibernateTag = tagRepository.save(hibernateTag);

        // Create test post with tags
        postWithTags = new Post();
        postWithTags.setTitle("Test Post with Tags");
        postWithTags.setContent("This is a test post content");
        postWithTags.setAuthor(testUser);
        postWithTags.setSlug("test-post-with-tags");
        postWithTags.setPublished(true);
        postWithTags.setTags(List.of(javaTag, springTag));
        postWithTags = postRepository.save(postWithTags);
    }

    @Test
    void createPostWithTags_shouldSaveTagsCorrectly() throws Exception {
        // Arrange
        PostCreateDTO postCreateDTO = new PostCreateDTO();
        postCreateDTO.setTitle("New Post with Tags");
        postCreateDTO.setContent("This is a new post content");
        postCreateDTO.setAuthorId(testUser.getId());
        postCreateDTO.setTagIds(List.of(javaTag.getId(), springTag.getId(), hibernateTag.getId()));
        postCreateDTO.setSlug("new-post-with-tags");
        postCreateDTO.setPublished(true);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Post with Tags")))
                .andExpect(jsonPath("$.tags", hasSize(3)))
                .andReturn();

        // Verify the response contains tags
        String responseBody = result.getResponse().getContentAsString();
        PostDTO createdPost = objectMapper.readValue(responseBody, PostDTO.class);
        assertEquals(3, createdPost.getTags().size());

        // Verify in database
        Post savedPost = postRepository.findByIdWithTags(createdPost.getId()).orElseThrow();
        assertEquals(3, savedPost.getTags().size());
        assertTrue(savedPost.getTags().stream()
                .anyMatch(tag -> tag.getName().equals("java")));
        assertTrue(savedPost.getTags().stream()
                .anyMatch(tag -> tag.getName().equals("spring")));
        assertTrue(savedPost.getTags().stream()
                .anyMatch(tag -> tag.getName().equals("hibernate")));
    }

    @Test
    void getPostWithTags_shouldReturnTagsInResponse() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/posts/{id}", postWithTags.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postWithTags.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Post with Tags")))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags[0].name", is(oneOf("java", "spring"))))
                .andExpect(jsonPath("$.tags[1].name", is(oneOf("java", "spring"))));
    }

    @Test
    void getAllPosts_shouldReturnPostsWithTags() throws Exception {
        // Arrange - Create another post
        Post anotherPost = new Post();
        anotherPost.setTitle("Another Post");
        anotherPost.setContent("Another content");
        anotherPost.setAuthor(testUser);
        anotherPost.setSlug("another-post");
        anotherPost.setPublished(true);
        anotherPost.setTags(List.of(javaTag));
        postRepository.save(anotherPost);

        // Act & Assert
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].tags").exists())
                .andExpect(jsonPath("$.content[1].tags").exists());
    }

    @Test
    void deleteUnusedTag_shouldSucceed() throws Exception {
        // Arrange - Create a tag that is not used by any post
        Tag unusedTag = new Tag();
        unusedTag.setName("unused");
        unusedTag = tagRepository.save(unusedTag);

        // Act & Assert
        mockMvc.perform(delete("/api/tags/{id}", unusedTag.getId()))
                .andExpect(status().isNoContent());

        // Verify tag was deleted
        assertFalse(tagRepository.existsById(unusedTag.getId()));
    }

    @Test
    void bulkCreateTags_shouldWorkCorrectly() throws Exception {
        // Arrange
        TagCreateDTO tag1 = new TagCreateDTO();
        tag1.setName("tag1");

        TagCreateDTO tag2 = new TagCreateDTO();
        tag2.setName("tag2");

        List<TagCreateDTO> tags = List.of(tag1, tag2);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/tags/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tags)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        // Verify response
        String responseBody = result.getResponse().getContentAsString();
        List<TagDTO> createdTags = objectMapper.readValue(responseBody, new TypeReference<List<TagDTO>>() {});
        assertEquals(2, createdTags.size());

        // Verify in database - initial 3 tags + 2 new ones = 5 total
        assertEquals(5, tagRepository.count());
        assertTrue(tagRepository.existsByName("tag1"));
        assertTrue(tagRepository.existsByName("tag2"));
    }

    @Test
    void getTagById_shouldReturnTag() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tags/{id}", javaTag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(javaTag.getId().intValue())))
                .andExpect(jsonPath("$.name", is("java")));
    }

    @Test
    void getAllTags_shouldReturnAllTags() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.name == 'java')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'spring')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'hibernate')]").exists());
    }

    @Test
    void createTag_shouldWorkCorrectly() throws Exception {
        // Arrange
        TagCreateDTO tagDTO = new TagCreateDTO();
        tagDTO.setName("new-tag");

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("new-tag")));

        // Verify in database
        assertEquals(4, tagRepository.count());
        assertTrue(tagRepository.existsByName("new-tag"));
    }

    @Test
    void updateTag_shouldWorkCorrectly() throws Exception {
        // Arrange
        TagUpdateDTO updateDTO = new TagUpdateDTO();
        updateDTO.setName(JsonNullable.of("updated-java"));

        // Act & Assert
        mockMvc.perform(patch("/api/tags/{id}", javaTag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("updated-java")));

        // Verify in database
        Tag updatedTag = tagRepository.findById(javaTag.getId()).orElseThrow();
        assertEquals("updated-java", updatedTag.getName());
    }

    @Test
    void findPostsByTag_shouldReturnFilteredPosts() throws Exception {
        // Act & Assert - Find posts by java tag
        mockMvc.perform(get("/api/posts?tagId={tagId}", javaTag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Test Post with Tags")))
                .andExpect(jsonPath("$.content[0].tags[?(@.name == 'java')]").exists());
    }
}
