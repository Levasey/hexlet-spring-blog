package io.hexletspringblog.repository;

import io.hexletspringblog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    /**
     * Одна коллекция + автор в одном запросе. Комментарии подгружаются отдельно ({@link #findByIdWithComments}),
     * иначе Hibernate не даст два JOIN FETCH по двум List-коллекциям (multiple bags).
     */
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.author WHERE p.id = :id")
    Optional<Post> findByIdWithTagsAndAuthor(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :id")
    Optional<Post> findByIdWithComments(@Param("id") Long id);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.author")
    List<Post> findAllWithTagsAndAuthor();

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id IN :ids")
    List<Post> findAllByIdWithTags(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.author WHERE p.id IN :ids")
    List<Post> findAllByIdWithTagsAndAuthor(@Param("ids") List<Long> ids);

    /**
     * Идентификаторы постов для пагинации без JOIN к коллекциям — корректные count, limit и offset.
     */
    @Query(value = "SELECT p.id FROM Post p",
            countQuery = "SELECT COUNT(p) FROM Post p")
    Page<Long> findAllPostIds(Pageable pageable);

    /**
     * Теги и автор подгружаются отдельным запросом по id страницы, чтобы не смешивать FETCH JOIN с пагинацией
     * (дубликаты сущностей и неверный total при нескольких тегах у одного поста).
     */
    default Page<Post> findAllWithTagsAndAuthor(Pageable pageable) {
        Page<Long> idPage = findAllPostIds(pageable);
        if (idPage.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, idPage.getTotalElements());
        }
        List<Post> loaded = findAllByIdWithTagsAndAuthor(idPage.getContent());
        Map<Long, Post> byId = loaded.stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        List<Post> ordered = idPage.getContent().stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();
        return new PageImpl<>(ordered, pageable, idPage.getTotalElements());
    }
}
