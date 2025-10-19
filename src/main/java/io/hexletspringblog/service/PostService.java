package io.hexletspringblog.service;

import io.hexletspringblog.dto.PostCreateDTO;
import io.hexletspringblog.dto.PostDTO;
import io.hexletspringblog.dto.PostUpdateDTO;
import io.hexletspringblog.exception.ResourceNotFoundException;
import io.hexletspringblog.mapper.PostMapper;
import io.hexletspringblog.model.Post;
import io.hexletspringblog.model.Tag;
import io.hexletspringblog.model.User;
import io.hexletspringblog.repository.PostRepository;
import io.hexletspringblog.repository.TagRepository;
import io.hexletspringblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public Page<PostDTO> findAll(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(postMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public PostDTO findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        return postMapper.toDTO(post);
    }

    public PostDTO create(PostCreateDTO postCreateDTO) {
        User user = userRepository.findById(postCreateDTO.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + postCreateDTO.getAuthorId()));

        Post post = postMapper.toEntity(postCreateDTO);
        post.setAuthor(user);

        // Handle tags if provided
        if (postCreateDTO.getTagIds() != null && !postCreateDTO.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(postCreateDTO.getTagIds());
            post.setTags(tags);
        }

        Post savedPost = postRepository.save(post);
        return postMapper.toDTO(savedPost);
    }

    public PostDTO update(Long id, PostUpdateDTO postUpdateDTO) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        postMapper.updateEntityFromDTO(postUpdateDTO, post);

        // Handle tags update if provided
        if (postUpdateDTO.getTagIds() != null && postUpdateDTO.getTagIds().isPresent()) {
            List<Tag> tags = tagRepository.findAllById(postUpdateDTO.getTagIds().get());
            post.setTags(tags);
        }

        Post updatedPost = postRepository.save(post);
        return postMapper.toDTO(updatedPost);
    }

    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }
}