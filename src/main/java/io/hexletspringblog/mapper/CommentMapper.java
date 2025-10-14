package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.CommentDTO;
import io.hexletspringblog.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(source = "post.id", target = "postId")
    CommentDTO toDTO(Comment comment);

    @Mapping(source = "postId", target = "post.id")
    Comment toEntity(CommentDTO commentDTO);
}
