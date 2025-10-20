package io.hexletspringblog.repository;

import io.hexletspringblog.mapper.TagMapper;
import io.hexletspringblog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    // Поиск постов по заголовку (частичное совпадение)
    List<Post> findByTitleContainingIgnoreCase(String title);

    // Поиск постов, содержащих определенный тег
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.id = :tagId")
    List<Post> findByTagId(@Param("tagId") Long tagId);

    // Поиск постов по нескольким тегам
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.id IN :tagIds")
    List<Post> findByTagIds(@Param("tagIds") List<Long> tagIds);

    // Проверка существования поста с заголовком
    boolean existsByTitle(String title);

    // Поиск по слагу (если есть поле slug)
    Optional<Post> findBySlug(String slug);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<Post> findByIdWithTags(@Param("id") Long id);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.author")
    List<Post> findAllWithTagsAndAuthor();

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id IN :ids")
    List<Post> findAllByIdWithTags(@Param("ids") List<Long> ids);

    // Пагинация с тегами
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.author")
    Page<Post> findAllWithTagsAndAuthor(Pageable pageable);
}
