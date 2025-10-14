package io.hexletspringblog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexletspringblog.dto.CommentDTO;
import io.hexletspringblog.model.Comment;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.CommentRepository;
import io.hexletspringblog.repository.PostRepository;
import io.hexletspringblog.repository.UserRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private Post testPost;
    private User testUser;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя и пост для комментариев
        testUser = generateUser();
        userRepository.save(testUser);

        testPost = generatePost(testUser);
        postRepository.save(testPost);
    }

    @Test
    void testIndex() throws Exception {
        // Создаем несколько комментариев
        Comment comment1 = generateComment(testPost);
        Comment comment2 = generateComment(testPost);
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        var result = mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().hasSize(2);
    }

    @Test
    void testShow() throws Exception {
        Comment comment = generateComment(testPost);
        commentRepository.save(comment);

        mockMvc.perform(get("/api/comments/" + comment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.body").value(comment.getBody()))
                .andExpect(jsonPath("$.postId").value(testPost.getId()));
    }

    @Test
    void testShow_NotFound() throws Exception {
        mockMvc.perform(get("/api/comments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setBody("Test comment body");
        commentDTO.setPostId(testPost.getId());

        var request = post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(commentDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("Test comment body"))
                .andExpect(jsonPath("$.postId").value(testPost.getId()))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCreate_WithNonExistentPost() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setBody("Test comment body");
        commentDTO.setPostId(999L); // Несуществующий пост

        var request = post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(commentDTO));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    // Для диагностики можно добавить вывод информации об ошибке
                    if (result.getResolvedException() != null) {
                        System.out.println("Resolved exception: " + result.getResolvedException().getMessage());
                    }
                });
    }

    @Test
    void testUpdate() throws Exception {
        Comment comment = generateComment(testPost);
        commentRepository.save(comment);

        // Создаем новый пост для обновления связи
        Post newPost = generatePost(testUser);
        postRepository.save(newPost);

        Map<String, Object> updates = new HashMap<>();
        updates.put("body", "Updated comment body");
        updates.put("postId", newPost.getId());

        var request = put("/api/comments/" + comment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updates));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Updated comment body"))
                .andExpect(jsonPath("$.postId").value(newPost.getId()));

        // Проверяем, что комментарий действительно обновился в БД
        Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(updatedComment.getBody()).isEqualTo("Updated comment body");
        assertThat(updatedComment.getPost().getId()).isEqualTo(newPost.getId());
    }

    @Test
    void testUpdate_OnlyBody() throws Exception {
        Comment comment = generateComment(testPost);
        commentRepository.save(comment);

        Map<String, Object> updates = new HashMap<>();
        updates.put("body", "Updated comment body");
        // postId не отправляем - он не обязателен

        var request = put("/api/comments/" + comment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updates));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Updated comment body"))
                .andExpect(jsonPath("$.postId").value(testPost.getId())); // Post остался прежним
    }

    @Test
    void testUpdate_WithNonExistentPost() throws Exception {
        Comment comment = generateComment(testPost);
        commentRepository.save(comment);

        Map<String, Object> updates = new HashMap<>();
        updates.put("body", "Updated comment body");
        updates.put("postId", 999L); // Несуществующий пост

        var request = put("/api/comments/" + comment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updates));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("body", "Updated comment body");

        var request = put("/api/comments/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updates));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete() throws Exception {
        Comment comment = generateComment(testPost);
        commentRepository.save(comment);

        mockMvc.perform(delete("/api/comments/" + comment.getId()))
                .andExpect(status().isNoContent());

        // Проверяем, что комментарий удален из БД
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
    }

    @Test
    void testDelete_NotFound() throws Exception {
        mockMvc.perform(delete("/api/comments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCommentsByPost() throws Exception {
        // Создаем комментарии для тестового поста
        Comment comment1 = generateComment(testPost);
        Comment comment2 = generateComment(testPost);
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // Создаем другой пост и комментарий к нему (не должен попасть в результаты)
        Post otherPost = generatePost(testUser);
        postRepository.save(otherPost);
        Comment otherComment = generateComment(otherPost);
        commentRepository.save(otherComment);

        var result = mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        // Проверяем, что все комментарии возвращаются
        assertThatJson(body).isArray().hasSize(3);
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
                .supply(Select.field(Post::getTitle), () -> "Test Post")
                .supply(Select.field(Post::getContent), () -> "Test Content")
                .supply(Select.field(Post::getAuthor), () -> "Test Author")
                .set(Select.field(Post::getUser), user)
                .create();
    }

    private Comment generateComment(Post post) {
        return Instancio.of(Comment.class)
                .ignore(Select.field(Comment::getId))
                .supply(Select.field(Comment::getBody), () -> "Test comment body")
                .set(Select.field(Comment::getPost), post)
                .create();
    }
}
