package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.TagCreateDTO;
import io.hexletspringblog.dto.TagDTO;
import io.hexletspringblog.dto.TagUpdateDTO;
import io.hexletspringblog.model.Tag;
import org.mapstruct.*;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TagMapper {

    TagDTO toTagDTO(Tag tag);

    Tag toTag(TagDTO tagDTO);

    Tag toTag(TagCreateDTO tagCreateDTO);

    void update(TagUpdateDTO tagUpdateDTO, @MappingTarget Tag tag);

    default String map(JsonNullable<String> value) {
        return value == null || !value.isPresent() ? null : value.get();
    }

    default JsonNullable<String> map(String value) {
        return value == null ? JsonNullable.undefined() : JsonNullable.of(value);
    }

    @AfterMapping
    default void afterUpdate(TagUpdateDTO tagUpdateDTO, @MappingTarget Tag tag) {
        // Handle JsonNullable fields explicitly if needed
        if (tagUpdateDTO.getName() != null && tagUpdateDTO.getName().isPresent()) {
            tag.setName(tagUpdateDTO.getName().get());
        }
    }
}