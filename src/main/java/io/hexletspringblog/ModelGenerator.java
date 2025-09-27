package io.hexletspringblog;

import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.PostRepository;
import io.hexletspringblog.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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
    public void generateData() {
        for (int i = 0; i < 5; i++) {
            // Create and save User
            var user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setEmail(faker.internet().emailAddress());

            // Convert Date to LocalDate safely
            Date birthdayDate = faker.date().birthday();
            user.setBirthday(birthdayDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());

            userRepository.save(user);

            // Create and save Post with shorter content
            var post = new Post();
            post.setTitle(faker.book().title());

            // Generate content that fits within 2-30 characters
            String shortContent = faker.lorem().characters(10, 30); // Generates 10-30 characters
            post.setContent(shortContent);

            post.setAuthor(faker.name().fullName());
            post.setPublished(faker.bool().bool());
            postRepository.save(post);
        }
    }
}