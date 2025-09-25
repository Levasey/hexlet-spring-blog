package io.hexletspringblog.repository;

import io.hexletspringblog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PostRepository extends JpaRepository<Post, Long> {
}
