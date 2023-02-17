package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TagDtoMapper;
import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.repository.JpaTagRepository;
import io.modicon.taskapp.domain.repository.JpaTaskRepository;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TagService {
    TagGetByIdWithTaskResponse getTagWithTasks(String tagName, String page, String limit);

    TagGetAllWithTaskExistedResponse getAllTagsWithExistedTasks();

    TagCreateResponse create(TagCreateRequest request);

    TagUpdateResponse update(TagUpdateRequest request);

    TagDeleteResponse delete(String tagName);

    @Transactional
    @RequiredArgsConstructor
    @Service
    class Base implements TagService {

        private final JpaTagRepository tagRepository;
        private final JpaTaskRepository taskRepository;
        private final TaskDtoMapper taskDtoMapper;
        private final TagDtoMapper tagDtoMapper;
        private final TaskFileService taskFileService;

        @Transactional(readOnly = true)
        @Override
        public TagGetByIdWithTaskResponse getTagWithTasks(String tagName, String page, String limit) {
            TagEntity tag = tagRepository.findById(tagName)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "tag [%s] not found", tagName));

            Optional<Field> fieldToSort = Arrays
                    .stream(TaskEntity.class.getDeclaredFields())
                    .filter(f -> f.getType().equals(PriorityType.class))
                    .findFirst();

            List<TaskEntity> tasks;
            tasks = fieldToSort.map(field -> taskRepository.findByTag(tag, PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit),
                            Sort.by(field.getName()))))
                    .orElseGet(() -> taskRepository.findByTag(tag, PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit))
                    ));

            return new TagGetByIdWithTaskResponse(tagDtoMapper.apply(tag), tasks.stream().map(taskDtoMapper).toList());
        }

        @Transactional(readOnly = true)
        @Override
        public TagGetAllWithTaskExistedResponse getAllTagsWithExistedTasks() {
            List<TagEntity> tags = tagRepository.findAllByTaskCountIsGreaterThan(0L);
            return new TagGetAllWithTaskExistedResponse(tags.stream().map(tagDtoMapper).toList());
        }

        @Override
        public TagCreateResponse create(TagCreateRequest request) {
            String tagName = request.getTag();
            if (tagRepository.existsById(tagName))
                throw exception(HttpStatus.BAD_REQUEST, "tag [%s] already exist", tagName);

            TagEntity tag = new TagEntity(tagName, 0L);
            tagRepository.save(tag);

            return new TagCreateResponse(tagDtoMapper.apply(tag));
        }

        @Override
        public TagUpdateResponse update(TagUpdateRequest request) {
            String updatedTagName = request.getUpdatedTag();
            TagEntity tag = tagRepository.findById(updatedTagName)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "tag [%s] not found", updatedTagName));

            String newTagName = request.getTag();
            if (tagRepository.existsById(newTagName))
                throw exception(HttpStatus.BAD_REQUEST, "tag [%s] already exist", newTagName);

            tag.setNewName(newTagName); // set new name

            return new TagUpdateResponse(tagDtoMapper.apply(tag));
        }

        @Override
        public TagDeleteResponse delete(String tagName) {
            TagEntity tag = tagRepository.findById(tagName)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "tag [%s] not found", tagName));

            List<TaskEntity> tasks = taskRepository.findByTag(tag);
            tasks.forEach(t -> taskFileService.deleteTaskFiles(t.getId()));
            taskRepository.deleteAll(tasks);
            tagRepository.delete(tag);

            return new TagDeleteResponse(tagDtoMapper.apply(tag));
        }
    }
}
