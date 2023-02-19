package io.modicon.taskapp;

import feign.FeignException;
import io.modicon.taskapp.client.TagClient;
import io.modicon.taskapp.client.TaskClient;
import io.modicon.taskapp.config.FeignBasedRestTest;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TagEntity;
import io.modicon.taskapp.utils.AuthUtils;
import io.modicon.taskapp.web.dto.TagDto;
import io.modicon.taskapp.web.dto.TaskDto;
import io.modicon.taskapp.web.interaction.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;

public class TagApiTest extends FeignBasedRestTest {

    @Autowired
    private AuthUtils auth;

    @Autowired
    private TaskClient taskClient;

    @Autowired
    private TagClient tagClient;

    private static final String WRONG = "wrong";

    @Test
    void GET_TAG_WITH_TASKS_should_returnCorrectData() {
        auth.register().login();

        TagEntity tag = supplyTag();

        var createdTask1 = createTask(tag.getTagName(), PriorityType.COMMON);
        var createdTask2 = createTask(tag.getTagName(), PriorityType.URGENT);
        var createdTask3 = createTask(tag.getTagName(), PriorityType.IMPORTANT);
        var createdTask4 = createTask(UUID.randomUUID().toString(), PriorityType.IMPORTANT);

        var response = tagClient.getTagWithTasks(tag.getTagName(), "0", "4");

        assertThat(response.getTag().tagName()).isEqualTo(tag.getTagName());
        assertThat(response.getTag().taskCount()).isEqualTo(3L);

        assertThat(response.getTasks()).isNotEmpty();
        assertThat(response.getTasks().size()).isEqualTo(3);
        assertThat(response.getTasks().get(0)).isEqualTo(createdTask2);
        assertThat(response.getTasks().get(1)).isEqualTo(createdTask3);
        assertThat(response.getTasks().get(2)).isEqualTo(createdTask1);
        assertThat(response.getTasks().contains(createdTask4)).isFalse();
    }

    @Test
    void GET_TAG_WITH_TASKS_should_return404_whenTagIsNotFoundByName() {
        auth.register().login();

        FeignException exception = catchThrowableOfType(() -> tagClient.getTagWithTasks(WRONG, "0", "4"), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void GET_TAG_WITH_TASKS_should_return401_whenUnauthorized() {
        auth.logout();
        FeignException exception = catchThrowableOfType(() -> tagClient.getTagWithTasks(WRONG, "0", "4"), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void GET_TAG_WITH_TASKS_should_returnMoreTasksWhenLoginAsAdmin() {
        auth.register().login();

        TagEntity tag = supplyTag();

        createTask(tag.getTagName(), PriorityType.COMMON);
        createTask(tag.getTagName(), PriorityType.URGENT);
        createTask(tag.getTagName(), PriorityType.IMPORTANT);
        createTask(UUID.randomUUID().toString(), PriorityType.IMPORTANT);

        auth.register().login();
        createTask(tag.getTagName(), PriorityType.COMMON);
        createTask(tag.getTagName(), PriorityType.URGENT);
        createTask(tag.getTagName(), PriorityType.IMPORTANT);
        createTask(UUID.randomUUID().toString(), PriorityType.IMPORTANT);

        var userResponse = tagClient.getTagWithTasks(tag.getTagName(), "0", "200").getTasks();

        auth.registerAdmin().login();
        var adminResponse = tagClient.getTagWithTasks(tag.getTagName(), "0", "200").getTasks();

        assertThat(userResponse.size()).isLessThan(adminResponse.size());
    }

    @Test
    void GET_ALL_TAGS_WITH_EXISTED_TASKS_should_returnCorrectData() {
        auth.register().login();

        TagEntity tag = supplyTag();

        createTask(tag.getTagName(), PriorityType.COMMON);
        createTask(tag.getTagName(), PriorityType.URGENT);
        createTask(tag.getTagName(), PriorityType.IMPORTANT);
        createTask(UUID.randomUUID().toString(), PriorityType.IMPORTANT);

        var response = tagClient.getAllTagsWithExistedTasks().getTags();

        assertThat(response).isNotEmpty();
        assertThat(response.contains(new TagDto(tag.getTagName(), 3L))).isTrue();
        assertThat(response.contains(new TagDto(tag.getTagName(), 0L))).isFalse();
    }

    @Test
    void GET_ALL_TAGS_WITH_EXISTED_TASKS_should_return401_whenUnauthorized() {
        FeignException exception = catchThrowableOfType(() -> tagClient.getAllTagsWithExistedTasks(), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void CREATE_TAG_should_returnCorrectData() {
        auth.register().login();

        TagEntity tag = supplyTag();

        var response = tagClient.create(new TagCreateRequest(tag.getTagName())).getTag();

        assertThat(response.tagName()).isEqualTo(tag.getTagName());
        assertThat(response.taskCount()).isEqualTo(0L);
    }

    @Test
    void CREATE_TAG_should_return400_whenAlreadyExistByName() {
        auth.register().login();

        TagEntity tag = supplyTag();
        var response = tagClient.create(new TagCreateRequest(tag.getTagName())).getTag();
        assertThat(response.tagName()).isEqualTo(tag.getTagName());
        assertThat(response.taskCount()).isEqualTo(0L);

        FeignException exception = catchThrowableOfType(() -> tagClient.create(new TagCreateRequest(tag.getTagName())), FeignException.class);
        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void CREATE_TAG_should_return401_whenUnauthorized() {
        auth.logout();
        TagEntity tag = supplyTag();
        FeignException exception = catchThrowableOfType(() -> tagClient.create(new TagCreateRequest(tag.getTagName())), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void UPDATE_TAG_should_returnCorrectData() {
        auth.registerAdmin().login();

        TagEntity tag = supplyTag();
        var createdTag = tagClient.create(new TagCreateRequest(tag.getTagName())).getTag();
        assertThat(createdTag.tagName()).isEqualTo(tag.getTagName());
        assertThat(createdTag.taskCount()).isEqualTo(0L);

        TagEntity newTag = supplyTag();
        var response = tagClient.update(tag.getTagName(), new TagUpdateRequest(null, null, newTag.getTagName())).getTag();
        assertThat(response.tagName()).isEqualTo(newTag.getTagName());
        assertThat(response.taskCount()).isEqualTo(0L);
    }

    @Test
    void UPDATE_TAG_should_return401_whenUnauthorized() {
        auth.registerAdmin().login();

        TagEntity tag = supplyTag();
        var createdTag = tagClient.create(new TagCreateRequest(tag.getTagName())).getTag();
        assertThat(createdTag.tagName()).isEqualTo(tag.getTagName());
        assertThat(createdTag.taskCount()).isEqualTo(0L);

        auth.logout();
        TagEntity newTag = supplyTag();
        FeignException exception = catchThrowableOfType(() -> tagClient.update(tag.getTagName(), new TagUpdateRequest(null, null, newTag.getTagName())), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void UPDATE_TAG_should_return401_whenTryToUpdateTagAsUser() {
        auth.registerAdmin().login();

        TagEntity tag = supplyTag();
        var createdTag = tagClient.create(new TagCreateRequest(tag.getTagName())).getTag();
        assertThat(createdTag.tagName()).isEqualTo(tag.getTagName());
        assertThat(createdTag.taskCount()).isEqualTo(0L);

        auth.register().login();
        TagEntity newTag = supplyTag();
        FeignException exception = catchThrowableOfType(() -> tagClient.update(tag.getTagName(), new TagUpdateRequest(null, null, newTag.getTagName())), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void UPDATE_TAG_should_return404_whenTagNotFoundByName() {
        auth.registerAdmin().login();

        TagEntity tag = supplyTag();
        TagEntity newTag = supplyTag();
        FeignException exception = catchThrowableOfType(() -> tagClient.update(tag.getTagName(), new TagUpdateRequest(null, null, newTag.getTagName())), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void DELETE_TAG_should_returnCorrectData() {
        auth.registerAdmin().login();

        TagEntity tag = supplyTag();
        var createdTag = tagClient.create(new TagCreateRequest(tag.getTagName())).getTag();
        assertThat(createdTag.tagName()).isEqualTo(tag.getTagName());
        assertThat(createdTag.taskCount()).isEqualTo(0L);

        var response = tagClient.delete(tag.getTagName()).getTag();
        assertThat(response.tagName()).isEqualTo(tag.getTagName());
        assertThat(response.taskCount()).isEqualTo(0L);
    }

    @Test
    void DELETE_TAG_should_return401_whenUnauthorized() {
        auth.registerAdmin().login();

        TagEntity tag = supplyTag();
        var createdTag = tagClient.create(new TagCreateRequest(tag.getTagName())).getTag();
        assertThat(createdTag.tagName()).isEqualTo(tag.getTagName());
        assertThat(createdTag.taskCount()).isEqualTo(0L);

        auth.logout();
        FeignException exception = catchThrowableOfType(() -> tagClient.delete(tag.getTagName()), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void DELETE_TAG_should_return401_whenTryToUpdateTagAsUser() {
        auth.registerAdmin().login();

        TagEntity tag = supplyTag();
        var createdTag = tagClient.create(new TagCreateRequest(tag.getTagName())).getTag();
        assertThat(createdTag.tagName()).isEqualTo(tag.getTagName());
        assertThat(createdTag.taskCount()).isEqualTo(0L);

        auth.register().login();
        FeignException exception = catchThrowableOfType(() -> tagClient.delete(tag.getTagName()), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void DELETE_TAG_should_return404_whenTagNotFoundByName() {
        auth.registerAdmin().login();

        TagEntity tag = supplyTag();
        FeignException exception = catchThrowableOfType(() -> tagClient.delete(tag.getTagName()), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    private TaskDto createTask(String tag, PriorityType type) {
        var request = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                type.name(), UUID.randomUUID().toString(),
                LocalDate.now(), tag
        );
        TaskCreateResponse response = taskClient.create(request);

        assertThat(response.getTask().id()).isEqualTo(request.getId());
        assertThat(response.getTask().description()).isEqualTo(request.getDescription());
        assertThat(response.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(response.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(response.getTask().tag()).isEqualTo(request.getTag());
        return response.getTask();
    }

    private TagEntity supplyTag() {
        return new TagEntity(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 0L);
    }
}
