package io.modicon.taskapp.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.modicon.taskapp.application.service.SecurityContextHolderService;
import io.modicon.taskapp.application.service.TagService;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.infrastructure.security.jwt.JwtAuthFilter;
import io.modicon.taskapp.web.dto.TagDto;
import io.modicon.taskapp.web.dto.TaskDto;
import io.modicon.taskapp.web.interaction.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TagController.class)
@AutoConfigureMockMvc(addFilters = false)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;
    @MockBean
    private JwtAuthFilter jwtAuthFilter;
    @MockBean
    private SecurityContextHolderService securityContextHolderService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    String BASE_URL = "/api/v1/tags";

    private final UserEntity creator;
    private final TaskEntity commonTask;
    private final TagEntity tag;
    private final TagDto tagDto;
    private final TaskDto taskDto;

    {
        creator = UserEntity.builder()
                .username("username")
                .password("password")
                .build();
        tag = new TagEntity("id1","tag", 1L);
        tagDto = new TagDto(tag.getTagName(), tag.getTaskCount());
        commonTask = TaskEntity.builder()
                .id("taskid2")
                .tag(tag)
                .description("description")
                .priorityType(PriorityType.COMMON)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .build();
        taskDto = TaskDto.builder()
                .id(commonTask.getId())
                .tag(tagDto.tagName())
                .description(commonTask.getDescription())
                .priorityType(commonTask.getPriorityType().name())
                .createdAt(commonTask.getCreatedAt())
                .finishDate(commonTask.getFinishDate())
                .build();
    }

    @Test
    void shouldGetTagWithTasks() throws Exception {
        // given
        TagGetByIdWithTaskResponse expected = new TagGetByIdWithTaskResponse(tagDto, List.of(taskDto));
        String limit = "10";
        String page = "1";
        when(tagService.getTagWithTasks(tag.getTagName(), page, limit)).thenReturn(expected);

        // when
        var json = mockMvc.perform(get(BASE_URL + "/" + tag.getTagName())
                        .param("limit", limit)
                        .param("page", page)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        var result = objectMapper.writeValueAsString(expected);
        assertEquals(result, json);
    }

    @Test
    void shouldGetTagsWithTasks() throws Exception {
        // given
        TagGetAllWithTaskExistedResponse expected = new TagGetAllWithTaskExistedResponse(List.of(tagDto));
        when(tagService.getAllTagsWithExistedTasks()).thenReturn(expected);

        // when
        var json = mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        var result = objectMapper.writeValueAsString(expected);
        assertEquals(result, json);
    }

    @Test
    void shouldCreateTag() throws Exception {
        // given
        TagCreateResponse expected = new TagCreateResponse(tagDto);
        TagCreateRequest request = new TagCreateRequest(tag.getTagName());
        when(tagService.create(request)).thenReturn(expected);

        // when
        var json = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        var result = objectMapper.writeValueAsString(expected);
        assertEquals(result, json);
    }

    @Test
    void shouldUpdateTag() throws Exception {
        // given
        TagUpdateResponse expected = new TagUpdateResponse(tagDto);
        TagUpdateRequest request = new TagUpdateRequest(creator, tagDto.tagName(), tagDto.tagName());
        when(tagService.update(any())).thenReturn(expected);

        // when
        var json = mockMvc.perform(put(BASE_URL + "/" + tagDto.tagName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        var result = objectMapper.writeValueAsString(expected);
        assertEquals(result, json);
    }

    @Test
    void shouldDeleteTag() throws Exception {
        // given
        TagDeleteResponse expected = new TagDeleteResponse(tagDto);
        when(tagService.delete(anyString(), any())).thenReturn(expected);

        // when
        var json = mockMvc.perform(delete(BASE_URL + "/" + tagDto.tagName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        var result = objectMapper.writeValueAsString(expected);
        assertEquals(result, json);
    }
}