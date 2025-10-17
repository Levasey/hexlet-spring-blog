package io.hexletspringblog.mapper;

import io.hexletspringblog.model.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

@Component
public class ReferenceMapper {

    @Autowired
    private EntityManager entityManager;

    public <T extends BaseEntity> T toEntity(Long id, Class<T> entityClass) {
        return id != null ? entityManager.find(entityClass, id) : null;
    }
}
