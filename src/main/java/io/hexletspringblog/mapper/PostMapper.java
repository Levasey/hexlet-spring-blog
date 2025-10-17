package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.PostCreateDTO;
import io.hexletspringblog.dto.PostDTO;
import io.hexletspringblog.dto.PostUpdateDTO;
import io.hexletspringblog.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = { JsonNullableMapper.class, ReferenceMapper.class },
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PostMapper {

    Post map(PostCreateDTO dto);

    @Mapping(source = "authorId", target = "author.id")
    Post map(PostDTO model);

    @Mapping(source = "author.id", target = "authorId")
    PostDTO map(Post model);

    void update(PostUpdateDTO dto, @MappingTarget Post model);
}
