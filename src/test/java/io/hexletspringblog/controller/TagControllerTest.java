package io.hexletspringblog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexletspringblog.dto.TagCreateDTO;
import io.hexletspringblog.model.Tag;
import io.hexletspringblog.repository.TagRepository;
import io.hexletspringblog.service.TagService;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagService tagService;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();
        objectMapper.registerModule(new JsonNullableModule());
    }

    @Test
    void getAllTags_shouldReturnListOfTags() throws Exception {
        // Arrange
        Tag tag1 = generateTag("spring-boot");
        Tag tag2 = generateTag("java");
        tagRepository.saveAll(List.of(tag1, tag2));

        // Act & Assert
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("spring-boot")))
                .andExpect(jsonPath("$[1].name", is("java")));
    }

    @Test
    void getTagById_withValidId_shouldReturnTag() throws Exception {
        // Arrange
        Tag tag = generateTag("spring-boot");
        Tag savedTag = tagRepository.save(tag);

        // Act & Assert
        mockMvc.perform(get("/api/tags/{id}", savedTag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedTag.getId().intValue())))
                .andExpect(jsonPath("$.name", is("spring-boot")));
    }

    @Test
    void createTag_withValidData_shouldCreateTag() throws Exception {
        // Arrange
        TagCreateDTO tagCreateDTO = new TagCreateDTO();
        tagCreateDTO.setName("new-tag");

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("new-tag")));

        // Verify in database
        List<Tag> tags = tagRepository.findAll();
        assertEquals(1, tags.size());
        assertEquals("new-tag", tags.get(0).getName());
    }

    @Test
    void createTag_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Arrange
        TagCreateDTO invalidTag = new TagCreateDTO();
        invalidTag.setName("a"); // Too short - less than 2 characters

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTag)))
                .andExpect(status().isUnprocessableEntity());

        // Verify nothing was saved
        assertEquals(0, tagRepository.count());
    }

    @Test
    void deleteTag_withValidId_shouldDeleteTag() throws Exception {
        // Arrange
        Tag tag = generateTag("to-delete");
        Tag savedTag = tagRepository.save(tag);

        // Act & Assert
        mockMvc.perform(delete("/api/tags/{id}", savedTag.getId()))
                .andExpect(status().isNoContent());

        // Verify deleted from database
        assertFalse(tagRepository.existsById(savedTag.getId()));
    }

    @Test
    void createTag_withEmptyName_shouldReturnBadRequest() throws Exception {
        // Arrange
        TagCreateDTO emptyNameTag = new TagCreateDTO();
        emptyNameTag.setName("");

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyNameTag)))
                .andExpect(status().isUnprocessableEntity());

        assertEquals(0, tagRepository.count());
    }

    @Test
    void createTag_withNullName_shouldReturnBadRequest() throws Exception {
        // Arrange
        TagCreateDTO nullNameTag = new TagCreateDTO();
        nullNameTag.setName(null);

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullNameTag)))
                .andExpect(status().isUnprocessableEntity());

        assertEquals(0, tagRepository.count());
    }

    @Test
    void index() throws Exception {
        // Arrange
        Tag tag1 = generateTag("spring-boot");
        Tag tag2 = generateTag("java");
        tagRepository.saveAll(List.of(tag1, tag2));

        // Act & Assert using json assertions
        var result = mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray().hasSize(2);
        assertThatJson(body).node("[0].name").isEqualTo("spring-boot");
        assertThatJson(body).node("[1].name").isEqualTo("java");
    }

    private Tag generateTag(String name) {
        return Instancio.of(Tag.class)
                .ignore(Select.field(Tag::getId))
                .ignore(Select.field(Tag::getPosts))
                .supply(Select.field(Tag::getName), () -> name)
                .create();
    }
}
