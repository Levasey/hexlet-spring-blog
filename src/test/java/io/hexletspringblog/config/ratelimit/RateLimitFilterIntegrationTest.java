package io.hexletspringblog.config.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
                "app.rate-limit.enabled=true",
                "app.rate-limit.login.enabled=true",
                "app.rate-limit.login.requests-per-minute=2",
                "app.rate-limit.public-get.enabled=true",
                "app.rate-limit.public-get.requests-per-minute=2",
        }
)
class RateLimitFilterIntegrationTest {

    private static final String EMPTY_LOGIN_JSON = "{\"username\":\"x\",\"password\":\"y\"}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_afterBurst_returns429WithRetryAfter() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(EMPTY_LOGIN_JSON))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EMPTY_LOGIN_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"));
    }

    @Test
    void publicGet_afterBurst_returns429() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(get("/api/tags")).andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"));
    }
}
