package io.hexletspringblog.controller;

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
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.CommentRepository;
import io.hexletspringblog.repository.PostRepository;
import io.hexletspringblog.repository.UserRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        // Очищаем базу данных перед каждым тестом
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        om.registerModule(new JsonNullableModule());
    }

    @Test
    void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    void testShow() throws Exception {

        var user = generateUser();
        userRepository.save(user);

        var request = get("/api/users/" + user.getId());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(user.getFirstName()),
                v -> v.node("lastName").isEqualTo(user.getLastName()),
                v -> v.node("email").isEqualTo(user.getEmail())
        );
    }

    @Test
    void createUser_returns201_andBody() throws Exception {
        User data = generateUser();

        var request = (post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data)));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value(data.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(data.getLastName()))
                .andExpect(jsonPath("$.email").value(data.getEmail()));
    }

    @Test
    void testUpdateUser_returns200_andBody() throws Exception {
        User user = generateUser();
        userRepository.save(user);

        var data = new HashMap<>();
        data.put("firstName", "newFirstName");
        data.put("lastName", user.getLastName());
        data.put("email", user.getEmail());

        var request = put("/api/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request).andExpect(status().isOk());

        user = userRepository.findById(user.getId()).get();

        assertThat(user.getFirstName()).isEqualTo(data.get("firstName"));
    }

    @Test
    void testDeleteUser_returns200_andBody() throws Exception {
        var user = generateUser();
        userRepository.save(user);

        var request = delete("/api/users/" + user.getId());

        mockMvc.perform(request).andExpect(status().isNoContent());

        user = userRepository.findById(user.getId()).orElse(null);

        assertThat(user).isNull();
    }

    private User generateUser() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> "John")
                .supply(Select.field(User::getLastName), () -> "Doe")
                .supply(Select.field(User::getEmail), () -> "john@example.com")
                .create();
    }
}
