package io.hexletspringblog;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.hexletspringblog.dto.PostUpdateDTO;
import io.hexletspringblog.mapper.PostMapper;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.PostRepository;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.datafaker.Faker;

@SpringBootTest
@AutoConfigureMockMvc
public class PostsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private Faker faker;

    private User testUser;
    private Post testPost;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(User.class)
                .ignore(Select.field(Post::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .create();

        testPost = Instancio.of(Post.class)
                .ignore(Select.field(Post::getId))
                .supply(Select.field(Post::getName), () -> faker.gameOfThrones().house())
                .supply(Select.field(Post::getBody), () -> faker.gameOfThrones().quote())
                .supply(Select.field(Post::getAuthor), () -> testUser)
                .create();
    }

    @Test
    public void testIndex() throws Exception {
        postRepository.save(testPost);

        var result = mockMvc.perform(get("/api/posts"))
                .andExpect(status()
                        .isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var dto = postMapper.map(testPost);

        var request = post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var post = postRepository.findBySlug(dto.getSlug()).get();
        assertNotNull(post);
        assertThat(post.getName()).isEqualTo(dto.getName());
    }

    @Test
    public void testUpdate() throws Exception {
        postRepository.save(testPost);

        var dto = new PostUpdateDTO();
        dto.setName(JsonNullable.of("new name"));

        var request = put("/api/posts/" + testPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var post = postRepository.findById(testPost.getId()).get();
        assertThat(post.getName()).isEqualTo(dto.getName());
    }

    @Test
    public void testShow() throws Exception {
        postRepository.save(testPost);

        var request = get("/api/posts/" + testPost.getId());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("slug").isEqualTo(testPost.getSlug()),
                v -> v.node("name").isEqualTo(testPost.getName()),
                v -> v.node("body").isEqualTo(testPost.getBody())
        );
    }

    @Test
    public void testDestroy() throws Exception {
        postRepository.save(testPost);
        var request = delete("/api/posts/" + testPost.getId());
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(postRepository.existsById(testPost.getId())).isEqualTo(false);
    }
}
