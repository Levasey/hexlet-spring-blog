package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.PostCreateDTO;
import io.hexletspringblog.dto.PostDTO;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.User;
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

        // Безопасная проверка на null для user
        if (post.getUser() != null) {
            dto.setUserId(post.getUser().getId());
        }

        // Маппим комментарии
        if (post.getComments() != null) {
            dto.setComments(post.getComments().stream()
                    .map(commentMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public Post toEntity(PostCreateDTO postCreateDTO) {
        Post post = new Post();
        post.setTitle(postCreateDTO.getTitle());
        post.setContent(postCreateDTO.getContent());
        // Остальные поля устанавливаются по умолчанию или в сервисе
        post.setPublished(false); // по умолчанию не опубликован

        // Создаем временного пользователя или устанавливаем связь

        if (postCreateDTO.getUserId() != null) {
            User user = new User();
            user.setId(postCreateDTO.getUserId());
            post.setUser(user);
        }

        return post;
    }
}
