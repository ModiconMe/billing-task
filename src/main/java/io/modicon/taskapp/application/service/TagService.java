package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.repository.TagRepository;
import io.modicon.taskapp.domain.repository.TaskRepository;
import io.modicon.taskapp.infrastructure.exception.ApiException;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TagService {
    TagGetByIdWithTaskResponse getTagWithTasks(String tagName);

    TagGetAllWithTaskExistedResponse getAllTagsWithExistedTasks();

    TagCreateResponse create(TagCreateRequest request);

    TagUpdateResponse update(TagUpdateRequest withUpdatedTag);

    TagDeleteResponse delete(String tagName);

    @RequiredArgsConstructor
    @Service
    class Base implements TagService {

        private final TagRepository tagRepository;
        private final TaskRepository taskRepository;
        private final TaskDtoMapper taskDtoMapper;

        @Override
        public TagGetByIdWithTaskResponse getTagWithTasks(String tagName) {
            TagEntity tag = tagRepository.findById(tagName)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "tag [%s] not found", tagName));

            List<TaskEntity> tasks = taskRepository.findByTagsContaining(tag);

            return new TagGetByIdWithTaskResponse(tagName, tasks.stream().map(taskDtoMapper).toList());
        }

        @Override
        public TagGetAllWithTaskExistedResponse getAllTagsWithExistedTasks() {
            return null;
        }

        @Override
        public TagCreateResponse create(TagCreateRequest request) {
            return null;
        }

        @Override
        public TagUpdateResponse update(TagUpdateRequest withUpdatedTag) {
            return null;
        }

        @Override
        public TagDeleteResponse delete(String tagName) {
            return null;
        }
    }
}
