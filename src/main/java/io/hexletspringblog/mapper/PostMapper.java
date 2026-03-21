package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.PostCreateDTO;
import io.hexletspringblog.dto.PostDTO;
import io.hexletspringblog.dto.PostUpdateDTO;
import io.hexletspringblog.model.Post;
import org.mapstruct.*;

@Mapper(
        uses = { JsonNullableMapper.class, ReferenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PostMapper {

    @Mapping(target = "authorId", source = "author.id")
    PostDTO toDTO(Post post);

    @Mapping(target = "author.id", source = "authorId")
    Post toEntity(PostCreateDTO dto);

    void updateEntityFromDTO(PostUpdateDTO dto, @MappingTarget Post post);
}
