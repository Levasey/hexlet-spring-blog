package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.CommentDTO;
import io.hexletspringblog.model.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDTO toDTO(Comment comment);

    Comment toEntity(CommentDTO commentDTO);
}
