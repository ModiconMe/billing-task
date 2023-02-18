package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TagDtoMapper;
import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.repository.JpaTagRepository;
import io.modicon.taskapp.domain.repository.JpaTaskRepository;
import io.modicon.taskapp.domain.repository.TagDataSource;
import io.modicon.taskapp.domain.repository.TaskDataSource;
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

        private final TagDataSource.Read readTagDataSource;
        private final TagDataSource.Write writeTagDataSource;
        private final TaskDataSource.Read readTaskDataSource;
        private final TaskDataSource.Write writeTaskDataSource;
        private final TaskFileService taskFileService;

        private final TaskDtoMapper taskDtoMapper;
        private final TagDtoMapper tagDtoMapper;

        @Transactional(readOnly = true)
        @Override
        public TagGetByIdWithTaskResponse getTagWithTasks(String tagName, String page, String limit) {
            TagEntity tag = readTagDataSource.findById(tagName);

            List<TaskEntity> tasks = readTaskDataSource.findByTag(tag, page, limit);

            return new TagGetByIdWithTaskResponse(tagDtoMapper.apply(tag), tasks.stream().map(taskDtoMapper).toList());
        }

        @Transactional(readOnly = true)
        @Override
        public TagGetAllWithTaskExistedResponse getAllTagsWithExistedTasks() {
            List<TagEntity> tags = readTagDataSource.findTagWithTasks(0L);
            return new TagGetAllWithTaskExistedResponse(tags.stream().map(tagDtoMapper).toList());
        }

        @Override
        public TagCreateResponse create(TagCreateRequest request) {
            String tagName = request.getTag();
            readTagDataSource.validateNotExist(tagName);

            TagEntity tag = new TagEntity(tagName, 0L);
            writeTagDataSource.save(tag);

            return new TagCreateResponse(tagDtoMapper.apply(tag));
        }

        @Override
        public TagUpdateResponse update(TagUpdateRequest request) {
            String updatedTagName = request.getUpdatedTag();
            TagEntity tag = readTagDataSource.findById(updatedTagName);

            String newTagName = request.getTag();
            readTagDataSource.validateNotExist(newTagName);

            tag.setNewName(newTagName); // set new name

            return new TagUpdateResponse(tagDtoMapper.apply(tag));
        }

        @Override
        public TagDeleteResponse delete(String tagName) {
            TagEntity tag = readTagDataSource.findById(tagName);

            List<TaskEntity> tasks = readTaskDataSource.findByTag(tag);
            tasks.forEach(t -> taskFileService.deleteTaskFiles(t.getId()));
            writeTaskDataSource.deleteAll(tasks);
            writeTagDataSource.delete(tag);

            return new TagDeleteResponse(tagDtoMapper.apply(tag));
        }
    }
}
