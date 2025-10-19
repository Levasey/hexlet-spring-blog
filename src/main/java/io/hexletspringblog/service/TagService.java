package io.hexletspringblog.service;

import io.hexletspringblog.dto.TagCreateDTO;
import io.hexletspringblog.dto.TagDTO;
import io.hexletspringblog.dto.TagUpdateDTO;
import io.hexletspringblog.mapper.TagMapper;
import io.hexletspringblog.model.Tag;
import io.hexletspringblog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Transactional(readOnly = true)
    public List<TagDTO> findAll() {
        return tagRepository.findAll().stream()
                .map(tagMapper::toTagDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TagDTO findById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));
        return tagMapper.toTagDTO(tag);
    }

    public TagDTO create(TagCreateDTO tagCreateDTO) {
        // Check for duplicate name
        if (tagRepository.existsByName(tagCreateDTO.getName())) {
            throw new IllegalArgumentException("Tag with name '" + tagCreateDTO.getName() + "' already exists");
        }

        Tag tag = tagMapper.toTag(tagCreateDTO);
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toTagDTO(savedTag);
    }

    public TagDTO update(Long id, TagUpdateDTO tagUpdateDTO) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));

        // Check if name is being updated and if new name already exists
        if (tagUpdateDTO.getName() != null && tagUpdateDTO.getName().isPresent()) {
            String newName = tagUpdateDTO.getName().get();
            if (!newName.equals(tag.getName()) && tagRepository.existsByName(newName)) {
                throw new IllegalArgumentException("Tag with name '" + newName + "' already exists");
            }
        }

        tagMapper.update(tagUpdateDTO, tag);
        Tag updatedTag = tagRepository.save(tag);
        return tagMapper.toTagDTO(updatedTag);
    }

    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));

        // Check if tag is used by any posts
        if (!tag.getPosts().isEmpty()) {
            throw new IllegalStateException("Cannot delete tag that is associated with posts. First remove the tag from all posts.");
        }

        tagRepository.delete(tag);
    }

    // Bulk operations
    public List<TagDTO> createBulk(List<TagCreateDTO> tagCreateDTOs) {
        List<Tag> tags = tagCreateDTOs.stream()
                .map(tagMapper::toTag)
                .toList();

        List<Tag> savedTags = tagRepository.saveAll(tags);
        return savedTags.stream()
                .map(tagMapper::toTagDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagDTO> findByIds(List<Long> ids) {
        return tagRepository.findByIdIn(ids).stream()
                .map(tagMapper::toTagDTO)
                .toList();
    }
}