package io.modicon.taskapp.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.modicon.taskapp.application.service.SecurityContextHolderService;
import io.modicon.taskapp.application.service.TaskService;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;
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

    String BASE_URL = "/api/v1/tasks";

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
    void shouldCreateTask() throws Exception {
        // given
        TaskCreateResponse expected = new TaskCreateResponse(taskDto);
        TaskCreateRequest request = new TaskCreateRequest(creator, taskDto.id(), taskDto.priorityType(),
                taskDto.description(), taskDto.finishDate().toString(), taskDto.tag());
        when(taskService.create(any())).thenReturn(expected);

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
    void shouldUpdateTask() throws Exception {
        // given
        TaskUpdateResponse expected = new TaskUpdateResponse(taskDto);
        TaskUpdateRequest request = new TaskUpdateRequest(creator, taskDto.priorityType(),
                taskDto.description(), taskDto.finishDate().toString(), taskDto.tag());
        when(taskService.update(anyString(), any())).thenReturn(expected);

        // when
        var json = mockMvc.perform(put(BASE_URL + "/" + taskDto.id())
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
    void shouldDeleteTask() throws Exception {
        // given
        TaskDeleteResponse expected = new TaskDeleteResponse(taskDto.id());
        when(taskService.delete(anyString(), any())).thenReturn(expected);

        // when
        var json = mockMvc.perform(delete(BASE_URL + "/" + taskDto.id())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        var result = objectMapper.writeValueAsString(expected);
        assertEquals(result, json);
    }

    @Test
    void shouldGetTasksByDate() throws Exception {
        // given
        TaskGetByDateResponse expected = new TaskGetByDateResponse(List.of(taskDto));
        String limit = "10";
        String page = "0";
        String date = LocalDate.now().toString();
        when(taskService.get(anyString(), anyString(), anyString(), any())).thenReturn(expected);

        // when
        var json = mockMvc.perform(get(BASE_URL + "/byDate")
                        .param("limit", limit)
                        .param("page", page)
                        .param("finish_date", date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        var result = objectMapper.writeValueAsString(expected);
        assertEquals(result, json);
    }

    @Test
    void shouldGetTasks() throws Exception {
        // given
        TaskGetGroupByPriorityType expected = new TaskGetGroupByPriorityType(Map.of(commonTask.getPriorityType(), List.of(taskDto)));
        String limit = "10";
        String page = "0";
        when(taskService.get(anyString(), anyString(), any())).thenReturn(expected);

        // when
        var json = mockMvc.perform(get(BASE_URL)
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

}