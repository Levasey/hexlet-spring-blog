package io.hexletspringblog.specification;

import io.hexletspringblog.dto.PostParamsDTO;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.PostRepository;
import io.hexletspringblog.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Контрактные проверки {@link PostSpecification}: Criteria API → SQL на H2 (как в {@code application-test}).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostSpecificationIntegrationTest {

    @Autowired
    private PostSpecification postSpecification;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User authorA;
    private User authorB;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();

        authorA = userRepository.save(user("Alice", "alice@example.com"));
        authorB = userRepository.save(user("Bob", "bob@example.com"));
    }

    @Test
    void build_nameCont_matchesTitleCaseInsensitively() {
        savePost("Learning Spring Boot", "slug-a", true, authorA);
        savePost("Pure Java Notes", "slug-b", true, authorA);

        PostParamsDTO params = new PostParamsDTO();
        params.setNameCont("SPRING");

        List<Post> found = postRepository.findAll(postSpecification.build(params));

        assertThat(found).extracting(Post::getTitle).containsExactly("Learning Spring Boot");
    }

    @Test
    void build_publishedAndAuthorId_combineWithAnd() {
        Post pPublishedA = savePost("A pub", "slug-pa", true, authorA);
        Post pDraftA = savePost("A draft", "slug-da", false, authorA);
        Post pPublishedB = savePost("B pub", "slug-pb", true, authorB);

        PostParamsDTO params = new PostParamsDTO();
        params.setAuthorId(authorA.getId());
        params.setPublished(true);

        Specification<Post> spec = postSpecification.build(params);
        List<Post> found = postRepository.findAll(spec);

        assertThat(found).extracting(Post::getId).containsExactly(pPublishedA.getId());
        assertThat(found).noneMatch(p -> p.getId().equals(pDraftA.getId()));
        assertThat(found).noneMatch(p -> p.getId().equals(pPublishedB.getId()));
    }

    @Test
    void build_createdAtRange_inclusiveBoundsAgainstDatabase() {
        Post oldPost = savePost("Old", "slug-old", true, authorA);
        entityManager.flush();
        entityManager.createNativeQuery(
                        "UPDATE posts SET created_at = :ts WHERE id = :id")
                .setParameter("ts", LocalDateTime.of(2020, 6, 15, 12, 0))
                .setParameter("id", oldPost.getId())
                .executeUpdate();
        entityManager.clear();

        PostParamsDTO params = new PostParamsDTO();
        params.setCreatedAtGt(LocalDate.of(2020, 6, 14));
        params.setCreatedAtLt(LocalDate.of(2020, 6, 16));

        List<Post> found = postRepository.findAll(postSpecification.build(params));

        assertThat(found).extracting(Post::getId).containsExactly(oldPost.getId());
    }

    @Test
    void build_createdAtUpTo_excludesNextDayMidnight() {
        Post edge = savePost("Edge", "slug-edge", true, authorA);
        entityManager.flush();
        LocalDateTime almostNextDay = LocalDateTime.of(2024, 3, 10, 23, 59, 59);
        entityManager.createNativeQuery(
                        "UPDATE posts SET created_at = :ts WHERE id = :id")
                .setParameter("ts", almostNextDay)
                .setParameter("id", edge.getId())
                .executeUpdate();
        entityManager.clear();

        PostParamsDTO params = new PostParamsDTO();
        params.setCreatedAtLt(LocalDate.of(2024, 3, 9));

        assertThat(postRepository.findAll(postSpecification.build(params))).isEmpty();

        params.setCreatedAtLt(LocalDate.of(2024, 3, 10));
        assertThat(postRepository.findAll(postSpecification.build(params)))
                .extracting(Post::getId)
                .containsExactly(edge.getId());
    }

    private static User user(String first, String email) {
        User u = new User();
        u.setFirstName(first);
        u.setLastName("Test");
        u.setEmail(email);
        u.setPasswordDigest("x");
        return u;
    }

    private Post savePost(String title, String slug, boolean published, User author) {
        Post p = new Post();
        p.setTitle(title);
        p.setContent("body");
        p.setSlug(slug);
        p.setPublished(published);
        p.setAuthor(author);
        return postRepository.save(p);
    }
}
