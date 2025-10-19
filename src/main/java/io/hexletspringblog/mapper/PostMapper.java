package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.PostCreateDTO;
import io.hexletspringblog.dto.PostDTO;
import io.hexletspringblog.dto.PostUpdateDTO;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.User;
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

    @Mapping(target = "author", source = "authorId")
    Post toEntity(PostCreateDTO dto);

    void updateEntityFromDTO(PostUpdateDTO dto, @MappingTarget Post post);

    default User mapAuthorIdToUser(Long authorId) {
        if (authorId == null) {
            return null;
        }
        User user = new User();
        user.setId(authorId);
        return user;
    }
}
