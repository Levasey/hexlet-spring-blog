package io.hexletspringblog.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexletspringblog.dto.PostCreateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Интеграционные проверки цепочки Spring Security: в профиле {@code test} security по умолчанию
 * выключен; здесь она явно включена через {@code spring.security.enabled=true}.
 */
@SpringBootTest(properties = "spring.security.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostsApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPosts_withoutToken_returns200() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk());
    }

    @Test
    void createPost_withoutToken_returns401() throws Exception {
        PostCreateDTO body = new PostCreateDTO();
        body.setAuthorId(1L);
        body.setSlug("ab");
        body.setTitle("abc");
        body.setContent("1234567890");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}
