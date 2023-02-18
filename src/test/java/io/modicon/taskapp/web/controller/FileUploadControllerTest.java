package io.modicon.taskapp.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.modicon.taskapp.application.service.FileManagementService;
import io.modicon.taskapp.application.service.TaskFileService;
import io.modicon.taskapp.application.service.TaskService;
import io.modicon.taskapp.domain.model.FileData;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.infrastructure.security.jwt.JwtAuthFilter;
import io.modicon.taskapp.web.interaction.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = FileUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskFileService taskFileService;
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    String BASE_URL = "/api/v1/files";

    private FileData fileData;
    private TaskEntity commonTask;
    private MockMultipartFile file;

    {
        fileData = new FileData("id", "name", "text/plain", "path");
        commonTask = TaskEntity.builder()
                .id("taskid2")
                .tag(new TagEntity("id1","tag", 1L))
                .description("description")
                .priorityType(PriorityType.COMMON)
                .createdAt(LocalDate.now())
                .finishDate(LocalDate.now())
                .build();
        file = new MockMultipartFile("file", fileData.getName(), "text/plain", new byte[1]);
    }

    @Test
    void shouldListFiles() throws Exception {
        // given
        TaskFileListResponse expected = new TaskFileListResponse(Map.of(fileData.getId(), fileData.getName()));
        when(taskFileService.listFiles(commonTask.getId())).thenReturn(expected);

        // when
        var json = mockMvc.perform(get(BASE_URL + "/" + commonTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        var result = objectMapper.writeValueAsString(expected);
        assertEquals(result, json);
    }

    @Test
    void shouldUploadFile() throws Exception {
        // given
        TaskFileUploadResponse expected = new TaskFileUploadResponse(fileData.getName());
        when(taskFileService.upload(new TaskFileUploadRequest(commonTask.getId(), any()))).thenReturn(expected);

        // when
        var json = mockMvc.perform(multipart(BASE_URL + "/" + commonTask.getId())
                        .file(file))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        var result = objectMapper.writeValueAsString(expected);
        assertEquals(result, json);
    }

    @Test
    void shouldDownloadFile() throws Exception {
        // given
        TaskFileDownloadResponse expected = new TaskFileDownloadResponse(file.getBytes(), fileData.getType());
        when(taskFileService.download(new TaskFileDownloadRequest(commonTask.getId(), any()))).thenReturn(expected);

        // when
        var bytes = mockMvc.perform(get(BASE_URL + "/" + commonTask.getId() + "/" + file.getName())
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsByteArray();

        // then
        assertEquals(expected.getFile()[0], bytes[0]);
    }
}