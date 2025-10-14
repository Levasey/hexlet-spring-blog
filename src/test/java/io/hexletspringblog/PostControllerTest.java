package io.hexletspringblog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexletspringblog.dto.PostCreateDTO;
import io.hexletspringblog.dto.PostPatchDTO;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.PostRepository;
import io.hexletspringblog.repository.UserRepository;
import org.instancio.Instancio;
import org.instancio.Select;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();

        om.registerModule(new JsonNullableModule());
    }

    @Test
    void testIndex() throws Exception {
        // First create a user
        User user = generateUser();
        userRepository.save(user);
        // Create some test data
        Post post = generatePost(user);
        postRepository.save(post);

        var result = mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        // Instead of checking if the entire response is an array,
        // check that the "content" field is an array
        assertThatJson(body).node("content").isArray();

        // Optional: verify that the content array contains our test post
        assertThatJson(body).node("content").isArray().hasSize(1);
    }

    @Test
    void listPublished_returns200_andPage() throws Exception {
        // Create some test data
        User user = generateUser();
        userRepository.save(user);

        Post post = generatePost(user);
        postRepository.save(post);

        mockMvc.perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    // Additional test methods you might want to add:

    @Test
    void testCreatePost() throws Exception {
        // First create a user
        User user = generateUser();
        userRepository.save(user);

        // Create PostCreateDTO with userId
        PostCreateDTO postCreateDTO = generatePostCreateDTO();
        postCreateDTO.setUserId(user.getId());

        var request = post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(postCreateDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(postCreateDTO.getTitle()))
                .andExpect(jsonPath("$.content").value(postCreateDTO.getContent()));
    }

    @Test
    void testShowPost() throws Exception {
        // First create a user
        User user = generateUser();
        userRepository.save(user);

        Post post = generatePost(user);
        postRepository.save(post);

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.title").value(post.getTitle()))
                .andExpect(jsonPath("$.content").value(post.getContent()));
    }

    @Test
    void testUpdatePost() throws Exception {
        // First create a user
        User user = generateUser();
        userRepository.save(user);

        Post post = generatePost(user);
        postRepository.save(post);

        // Use PostCreateDTO for updates with userId
        PostCreateDTO updates = generatePostCreateDTO();
        updates.setTitle("Updated Title");
        updates.setContent("Updated content");
        updates.setUserId(user.getId()); // Устанавливаем userId

        var request = put("/api/posts/" + post.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updates));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    void testPatch() throws Exception {
        // First create a user
        User user = generateUser();
        userRepository.save(user);

        Post post = generatePost(user);
        postRepository.save(post);

        PostPatchDTO postPatchDTO = new PostPatchDTO();
        postPatchDTO.setTitle(JsonNullable.of("Updated Title"));

        var request = patch("/api/posts/" + post.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(postPatchDTO));

        mockMvc.perform(request).andExpect(status().isOk());

        post = postRepository.findById(post.getId()).get();

        assertThat(post.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void testDeletePost() throws Exception {
        // First create a user
        User user = generateUser();
        userRepository.save(user);

        Post post = generatePost(user);
        postRepository.save(post);

        mockMvc.perform(delete("/api/posts/" + post.getId()))
                .andExpect(status().isNoContent());

        // Verify the post was deleted
        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    private User generateUser() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> "John")
                .supply(Select.field(User::getLastName), () -> "Doe")
                .supply(Select.field(User::getEmail), () -> "john@example.com")
                .create();
    }

    private Post generatePost(User user) {
        return Instancio.of(Post.class)
                .ignore(Select.field(Post::getId))
                .ignore(Select.field(Post::getComments))
                .supply(Select.field(Post::getTitle), () -> "title")
                .supply(Select.field(Post::getContent), () -> "content content")
                .set(Select.field(Post::getAuthor), user)
                .create();
    }

    private PostCreateDTO generatePostCreateDTO() {
        PostCreateDTO dto = new PostCreateDTO();
        dto.setTitle("Test Title");
        dto.setContent("Test content for the post");
        return dto;
    }
}
