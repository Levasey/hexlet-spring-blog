package io.hexletspringblog.specification;

import io.hexletspringblog.dto.PostParamsDTO;
import io.hexletspringblog.model.Post;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Component //Для возможности автоматической инъекции
public class PostSpecification {
    // Генерация спецификации на основе параметров внутри DTO
    // Для удобства каждый фильтр вынесен в свой метод
    public Specification<Post> build(PostParamsDTO params) {
        return withTitleContaining(params.getNameCont())
                .and(withAuthorId(params.getAuthorId()))
                .and(withCreatedAtFrom(params.getCreatedAtGt()))
                .and(withCreatedAtUpTo(params.getCreatedAtLt()))
                .and(withPublished(params.getPublished()));
    }

    /** nameCont — подстрока в заголовке (без учёта регистра). */
    private Specification<Post> withTitleContaining(String nameCont) {
        return (root, query, cb) -> {
            if (nameCont == null || nameCont.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + nameCont.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get("title")), pattern);
        };
    }

    private Specification<Post> withAuthorId(Long authorId) {
        return (root, query, cb) ->
                authorId == null ? cb.conjunction() : cb.equal(root.get("author").get("id"), authorId);
    }

    private Specification<Post> withPublished(Boolean published) {
        return (root, query, cb) -> {
            if (published == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("published"), published);
        };
    }

    /**
     * Нижняя граница по календарной дате: {@code createdAt >= date} с 00:00:00.
     * Поле в сущности — {@link LocalDateTime}, сравнение через начало суток.
     */
    private Specification<Post> withCreatedAtFrom(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            LocalDateTime start = date.atStartOfDay();
            return cb.greaterThanOrEqualTo(root.get("createdAt"), start);
        };
    }

    /**
     * Верхняя граница по календарной дате включительно: все моменты до конца {@code date}
     * ({@code createdAt < date.plusDays(1).atStartOfDay()}).
     */
    private Specification<Post> withCreatedAtUpTo(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            LocalDateTime exclusiveEnd = date.plusDays(1).atStartOfDay();
            return cb.lessThan(root.get("createdAt"), exclusiveEnd);
        };
    }
}
