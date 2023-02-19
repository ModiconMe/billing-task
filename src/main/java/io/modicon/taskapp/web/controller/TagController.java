package io.modicon.taskapp.web.controller;

import io.modicon.taskapp.application.service.SecurityContextHolderService;
import io.modicon.taskapp.application.service.TagService;
import io.modicon.taskapp.domain.model.UserEntity;
import io.modicon.taskapp.web.dto.ApiExceptionDto;
import io.modicon.taskapp.web.interaction.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

public interface TagController {

    String BASE_URL_V1 = "api/v1/tags";

    @Operation(summary = "get tag and its task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully return data",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskUpdateResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "tag not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @GetMapping("/{tagName}")
    TagGetByIdWithTaskResponse getTagWithTasks(@PathVariable String tagName,
                                               @RequestParam(value = "page", defaultValue = "0") String page,
                                               @RequestParam(value = "limit", defaultValue = "20") String limit);

    @Operation(summary = "get tag with task count > 0")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully return data",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskUpdateResponse.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @GetMapping
    TagGetAllWithTaskExistedResponse getAllTagsWithExistedTasks();

    @Operation(summary = "create tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TagCreateResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "tag already exists or invalid data provided",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @PostMapping
    TagCreateResponse create(@Valid @RequestBody TagCreateRequest request);

    @Operation(summary = "update tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TagUpdateResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "tag already exists or invalid data provided",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "404", description = "tag not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
    @PutMapping("/{tagName}")
    TagUpdateResponse update(@PathVariable String tagName, @Valid @RequestBody TagUpdateRequest request);

    @Operation(summary = "delete tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TagDeleteResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "tag not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiExceptionDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Authentication error.",
                    content = @Content)
    })
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
            return tagService.getTagWithTasks(tagName, page, limit, securityContextHolderService.getCurrentUser());
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
