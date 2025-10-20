package io.hexletspringblog.util;

import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.PostRepository;
import io.hexletspringblog.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class ModelGenerator {

    private final Faker faker;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public ModelGenerator(Faker faker, UserRepository userRepository, PostRepository postRepository) {
        this.faker = faker;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @PostConstruct
    @Profile("!test")
    public void generateData() {
        Set<String> usedSlugs = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            // Create and save User
            var user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setEmail(faker.internet().emailAddress());

            // Generate birthday - simple approach
            int age = faker.number().numberBetween(18, 80);
            LocalDate birthday = LocalDate.now().minusYears(age)
                    .plusDays(faker.number().numberBetween(-365, 365));
            user.setBirthday(birthday);

            userRepository.save(user);

            // Create and save Post with unique slug
            var post = new Post();
            post.setAuthor(user);
            post.setTitle(faker.book().title());

            // Generate unique slug
            String slug;
            int attempt = 0;
            do {
                String baseSlug = generateSlug(faker.book().title());
                slug = attempt == 0 ? baseSlug : baseSlug + "-" + attempt;
                attempt++;
            } while (usedSlugs.contains(slug) && attempt < 10);

            usedSlugs.add(slug);
            post.setSlug(slug);

            // Generate content that fits within constraints
            String shortContent = faker.lorem().characters(10, 30);
            post.setContent(shortContent);
            post.setPublished(faker.bool().bool());
            postRepository.save(post);
        }
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
                .trim();
    }
}
