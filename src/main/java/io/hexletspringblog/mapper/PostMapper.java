package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.PostDTO;
import io.hexletspringblog.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PostMapper {

    @Autowired
    private CommentMapper commentMapper;

    public PostDTO toDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthor(post.getAuthor());
        dto.setPublished(post.isPublished());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setUserId(post.getUser().getId());

        // Маппим комментарии
        if (post.getComments() != null) {
            dto.setComments(post.getComments().stream()
                    .map(commentMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
