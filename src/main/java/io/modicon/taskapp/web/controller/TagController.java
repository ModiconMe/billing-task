package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.TagService;
import io.modicon.taskapp.web.interaction.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

public interface TagController {

    String BASE_URL_V1 = "api/v1/tags";

    @GetMapping("/{tagName}")
    TagGetByIdWithTaskResponse getTagWithTasks(@PathVariable String tagName);

    @GetMapping
    TagGetAllWithTaskExistedResponse getAllTagsWithExistedTasks();

    @PostMapping
    TagCreateResponse create(@Valid @RequestBody TagCreateRequest request);

    @PutMapping("/{tagName}")
    TagUpdateResponse update(@PathVariable String tagName, @Valid @RequestBody TagUpdateRequest request);

    @DeleteMapping("/{tagName}")
    TagDeleteResponse delete(@PathVariable String tagName);

    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class Base implements TagController {

        private final TagService tagService;

        @Override
        public TagGetByIdWithTaskResponse getTagWithTasks(String tagName) {
            return tagService.getTagWithTasks(tagName);
        }

        @Override
        public TagGetAllWithTaskExistedResponse getAllTagsWithExistedTasks() {
            return tagService.getAllTagsWithExistedTasks();
        }

        @Override
        public TagCreateResponse create(TagCreateRequest request) {
            return tagService.create(request);
        }

        @Override
        public TagUpdateResponse update(String tagName, TagUpdateRequest request) {
            return tagService.update(request.withUpdatedTag(tagName));
        }

        @Override
        public TagDeleteResponse delete(String tagName) {
            return tagService.delete(tagName);
        }
    }

}
