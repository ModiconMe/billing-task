package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.SecurityContextHolderService;
import io.modicon.taskapp.application.service.TagService;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.web.interaction.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

public interface TagController {

    String BASE_URL_V1 = "api/v1/tags";

    @GetMapping("/{tagName}")
    TagGetByIdWithTaskResponse getTagWithTasks(@PathVariable String tagName,
                                               @RequestParam(value = "page", defaultValue = "0") String page,
                                               @RequestParam(value = "limit", defaultValue = "20") String limit);

    @GetMapping
    TagGetAllWithTaskExistedResponse getAllTagsWithExistedTasks();

    @PostMapping
    TagCreateResponse create(@Valid @RequestBody TagCreateRequest request);

    @PutMapping("/{tagName}")
    TagUpdateResponse update(@PathVariable String tagName, @Valid @RequestBody TagUpdateRequest request);

    @DeleteMapping("/{tagName}")
    TagDeleteResponse delete(@PathVariable String tagName);

    @SecurityRequirement(name = "Bearer Authentication")
    @RequiredArgsConstructor
    @RestController
    @RequestMapping(BASE_URL_V1)
    class BaseTagController implements TagController {

        private final TagService tagService;
        private final SecurityContextHolderService securityContextHolderService;

        @Override
        public TagGetByIdWithTaskResponse getTagWithTasks(String tagName, String page, String limit) {
            return tagService.getTagWithTasks(tagName, page, limit);
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
            return tagService.update(request.withUpdatedTag(tagName).withUser(securityContextHolderService.getCurrentUser()));
        }

        @Override
        public TagDeleteResponse delete(String tagName) {
            return tagService.delete(tagName, securityContextHolderService.getCurrentUser());
        }
    }

}
