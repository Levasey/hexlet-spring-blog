package io.hexletspringblog.repository;

import io.hexletspringblog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    // Метод findAll с пагинацией уже доступен через JpaRepository
    Page<Post> findAll(Pageable pageable);

    Optional<Post> findBySlug(String slug);
}
