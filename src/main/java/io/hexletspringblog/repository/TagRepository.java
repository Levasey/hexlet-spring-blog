package io.hexletspringblog.repository;

import io.hexletspringblog.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT t FROM Tag t WHERE t.name IN :names")
    List<Tag> findByNames(@Param("names") List<String> names);

    List<Tag> findByIdIn(List<Long> ids);
}
