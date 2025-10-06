package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.CommentDTO;
import io.hexletspringblog.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentDTO toDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setBody(comment.getBody());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setPostId(comment.getPost().getId()); // Получаем ID поста из связи
        return dto;
    }

    public Comment toEntity(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setId(commentDTO.getId());
        comment.setBody(commentDTO.getBody());
        comment.setCreatedAt(commentDTO.getCreatedAt());
        // Note: post устанавливается отдельно через setPost()
        return comment;
    }
}
