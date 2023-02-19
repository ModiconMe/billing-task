package io.modicon.taskapp.application.service;

import io.modicon.taskapp.application.mapper.TagDtoMapper;
import io.modicon.taskapp.application.mapper.TaskDtoMapper;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.domain.model.TaskEntity;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.domain.repository.TagDataSource;
import io.modicon.taskapp.domain.repository.TaskDataSource;
import io.modicon.taskapp.infrastructure.security.ApplicationUserRole;
import io.modicon.taskapp.web.interaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TagService {
    TagGetByIdWithTaskResponse getTagWithTasks(String tagName, String page, String limit, UserEntity user);

    TagGetAllWithTaskExistedResponse getAllTagsWithExistedTasks();

    TagCreateResponse create(TagCreateRequest request);

    TagUpdateResponse update(TagUpdateRequest request);

    TagDeleteResponse delete(String tagName, UserEntity user);

    @Transactional
    @RequiredArgsConstructor
    @Service
    class Base implements TagService {

        private final TagDataSource.Read readTagDataSource;
        private final TagDataSource.Write writeTagDataSource;
        private final TaskDataSource.ReadUser readUserTaskDataSource;
        private final TaskDataSource.ReadAdmin readAdminTaskDataSource;
        private final TaskDataSource.Write writeTaskDataSource;
        private final TaskFileService taskFileService;

        private final TaskDtoMapper taskDtoMapper;
        private final TagDtoMapper tagDtoMapper;

        @Transactional(readOnly = true)
        @Override
        public TagGetByIdWithTaskResponse getTagWithTasks(String tagName, String page, String limit, UserEntity user) {
            TagEntity tag = readTagDataSource.findByName(tagName);

            List<TaskEntity> tasks;
            if (user.getRole().equals(ApplicationUserRole.ADMIN))
                tasks = readAdminTaskDataSource.findByTag(tag, page, limit);
            else
                tasks = readUserTaskDataSource.findByTag(tag, page, limit, user);

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

            TagEntity tag = new TagEntity(UUID.randomUUID().toString(), tagName, 0L);
            writeTagDataSource.save(tag);

            return new TagCreateResponse(tagDtoMapper.apply(tag));
        }

        @Override
        public TagUpdateResponse update(TagUpdateRequest request) {
            if (!request.getUser().getRole().equals(ApplicationUserRole.ADMIN))
                throw exception(HttpStatus.FORBIDDEN, "you are not allow to do this operation");

            String updatedTagName = request.getUpdatedTag();
            TagEntity tag = readTagDataSource.findByName(updatedTagName);

            String newTagName = request.getTag();
            readTagDataSource.validateNotExist(newTagName);

            tag.setNewName(newTagName); // set new name
            writeTagDataSource.save(tag);

            return new TagUpdateResponse(tagDtoMapper.apply(tag));
        }

        @Override
        public TagDeleteResponse delete(String tagName, UserEntity user) {
            if (!user.getRole().equals(ApplicationUserRole.ADMIN))
                throw exception(HttpStatus.FORBIDDEN, "you are not allow to do this operation");

            TagEntity tag = readTagDataSource.findByName(tagName);

            List<TaskEntity> tasks = readAdminTaskDataSource.findByTag(tag);
            tasks.forEach(t -> taskFileService.deleteTaskFiles(t.getId()));
            writeTaskDataSource.deleteAll(tasks);
            writeTagDataSource.delete(tag);

            return new TagDeleteResponse(tagDtoMapper.apply(tag));
        }
    }
}
