package io.modicon.taskapp;

import feign.FeignException;
import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.client.TaskClient;
import io.modicon.taskapp.config.FeignBasedRestTest;
import io.modicon.taskapp.utils.AuthUtils;
import io.modicon.taskapp.web.interaction.TaskCreateRequest;
import io.modicon.taskapp.web.interaction.TaskCreateResponse;
import io.modicon.taskapp.web.interaction.TaskUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;

public class TaskApiTest extends FeignBasedRestTest {

    @Autowired
    private AuthUtils auth;

    @Autowired
    private TaskClient taskClient;

    private static final String WRONG = "wrong";

    private static final String UPDATED_DESCRIPTION = "new description";
    private static final String UPDATED_PRIORITY_TYPE = PriorityType.URGENT.name();
    private static final LocalDate UPDATED_FINISH_DATE = LocalDate.now().plusDays(2);
    private static final String UPDATED_TAG = "new tag";

    @Test
    void CREATE_TASK_should_returnCorrectData() {
        auth.register().login();

        var request = createTask();
        TaskCreateResponse response = taskClient.create(request);

        assertThat(response.getTask().id()).isEqualTo(request.getId());
        assertThat(response.getTask().description()).isEqualTo(request.getDescription());
        assertThat(response.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(response.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(response.getTask().tag()).isEqualTo(request.getTag());
    }

    @Test
    void CREATE_TASK_should_return401_whenUnauthorized() {
        auth.logout();

        var request = createTask();
        FeignException exception = catchThrowableOfType(() -> taskClient.create(request), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void CREATE_TASK_should_return400_whenWrongDateProvided() {
        auth.register().login();

        var request = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.COMMON.name(), UUID.randomUUID().toString(),
                LocalDate.now().minusDays(1), UUID.randomUUID().toString());

        FeignException exception = catchThrowableOfType(() -> taskClient.create(request), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void CREATE_TASK_should_return400_whenWrongPriorityType() {
        auth.register().login();

        var request = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.COMMON.name() + WRONG, UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());

        FeignException exception = catchThrowableOfType(() -> taskClient.create(request), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void CREATE_TASK_should_return400_whenTaskAlreadyExist() {
        auth.register().login();

        var request = createTask();

        var response = taskClient.create(request);
        assertThat(response.getTask().id()).isEqualTo(request.getId());
        assertThat(response.getTask().description()).isEqualTo(request.getDescription());
        assertThat(response.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(response.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(response.getTask().tag()).isEqualTo(request.getTag());

        FeignException exception = catchThrowableOfType(() -> taskClient.create(request), FeignException.class);
        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void UPDATE_TASK_should_returnCorrectData() {
        auth.register().login();

        var request = createTask();

        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        var response = taskClient.update(request.getId(), updateTask());
        assertThat(response.getTask().id()).isEqualTo(request.getId());
        assertThat(response.getTask().description()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(response.getTask().finishDate()).isEqualTo(UPDATED_FINISH_DATE);
        assertThat(response.getTask().priorityType()).isEqualTo(UPDATED_PRIORITY_TYPE);
        assertThat(response.getTask().tag()).isEqualTo(UPDATED_TAG);
    }

    @Test
    void UPDATE_TASK_should_return401_whenUnauthorized() {
        auth.register().login();

        var request = createTask();

        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        auth.logout();

        FeignException exception = catchThrowableOfType(() -> taskClient.update(request.getId(), updateTask()), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void UPDATE_TASK_should_return404_whenTaskNotExist() {
        auth.register().login();

        var request = createTask();

        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        FeignException exception = catchThrowableOfType(() -> taskClient.update(request.getId() + WRONG, updateTask()), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void UPDATE_TASK_should_return400_whenWrongDateProvided() {
        auth.register().login();

        var request = createTask();

        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        FeignException exception = catchThrowableOfType(() -> taskClient.update(request.getId(), new TaskUpdateRequest(null, UPDATED_PRIORITY_TYPE,
                UPDATED_DESCRIPTION, LocalDate.now().minusDays(1), UPDATED_TAG)), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void UPDATE_TASK_should_return400_whenWrongPriorityTypeProvided() {
        auth.register().login();

        var request = createTask();

        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        FeignException exception = catchThrowableOfType(() -> taskClient.update(request.getId(), new TaskUpdateRequest(null, UPDATED_PRIORITY_TYPE + WRONG,
                UPDATED_DESCRIPTION, UPDATED_FINISH_DATE, UPDATED_TAG)), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void UPDATE_TASK_should_return404_whenTryToUpdateOtherUserTaskAsUserRole() {
        auth.register().login();
        var request = createTask();
        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        auth.register().login();
        FeignException exception = catchThrowableOfType(() -> taskClient.update(request.getId(), new TaskUpdateRequest(null, UPDATED_PRIORITY_TYPE,
                UPDATED_DESCRIPTION, UPDATED_FINISH_DATE, UPDATED_TAG)), FeignException.class);
        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void UPDATE_TASK_should_returnCorrectData_whenTryToUpdateOtherUserTaskAsAdminRole() {
        auth.register().login();
        var request = createTask();
        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        auth.registerAdmin().login();
        var response = taskClient.update(request.getId(), updateTask());
        assertThat(response.getTask().id()).isEqualTo(request.getId());
        assertThat(response.getTask().description()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(response.getTask().finishDate()).isEqualTo(UPDATED_FINISH_DATE);
        assertThat(response.getTask().priorityType()).isEqualTo(UPDATED_PRIORITY_TYPE);
        assertThat(response.getTask().tag()).isEqualTo(UPDATED_TAG);
    }

    @Test
    void DELETE_TASK_should_returnCorrectData() {
        auth.register().login();
        var request = createTask();
        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        var response = taskClient.delete(request.getId());
        assertThat(response.getId()).isEqualTo(request.getId());
    }

    @Test
    void DELETE_TASK_should_return404_whenTaskNotFound() {
        auth.register().login();
        var request = createTask();
        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        FeignException exception = catchThrowableOfType(() -> taskClient.delete(request.getId() + WRONG), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void DELETE_TASK_should_return404_whenTryToDeleteOtherUserTaskAsUserRole() {
        auth.register().login();
        var request = createTask();
        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        auth.register().login();
        FeignException exception = catchThrowableOfType(() -> taskClient.delete(request.getId()), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void DELETE_TASK_should_returnCorrectData_whenTryToDeleteOtherUserTaskAsAdminRole() {
        auth.register().login();
        var request = createTask();
        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        auth.registerAdmin().login();
        var response = taskClient.delete(request.getId());
        assertThat(response.getId()).isEqualTo(request.getId());
    }

    @Test
    void GET_TASK_BY_DATE_should_returnCorrectData() {
        auth.register().login();
        var request = createTask();
        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        var response = taskClient.get(request.getFinishDate().toString(), "0", "2");
        assertThat(response.getTasks()).isNotEmpty();
    }

    @Test
    void GET_TASK_BY_DATE_should_return400_whenWrongDateFormatProvided() {
        auth.register().login();
        var request = createTask();
        var createdTask = taskClient.create(request);
        assertThat(createdTask.getTask().id()).isEqualTo(request.getId());
        assertThat(createdTask.getTask().description()).isEqualTo(request.getDescription());
        assertThat(createdTask.getTask().finishDate()).isEqualTo(request.getFinishDate());
        assertThat(createdTask.getTask().priorityType()).isEqualTo(request.getPriorityType());
        assertThat(createdTask.getTask().tag()).isEqualTo(request.getTag());

        FeignException exception = catchThrowableOfType(() -> taskClient.get(request.getFinishDate().toString() + WRONG, "0", "2"), FeignException.class);

        assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.contentUTF8()).isNotEmpty();
    }

    @Test
    void GET_TASK_BY_DATE_should_returnEmptyList_whenTasksNotFound() {
        auth.register().login();

        var response = taskClient.get(LocalDate.now().plusDays(100).toString(), "0", "2").getTasks();

        assertThat(response).isEmpty();
    }

    @Test
    void GET_TASK_BY_DATE_should_returnMoreTasksWhenLoginAsAdmin() {
        auth.register().login();
        var request1 = createTask();
        var createdTask1 = taskClient.create(request1);
        assertThat(createdTask1.getTask().id()).isEqualTo(request1.getId());
        assertThat(createdTask1.getTask().description()).isEqualTo(request1.getDescription());
        assertThat(createdTask1.getTask().finishDate()).isEqualTo(request1.getFinishDate());
        assertThat(createdTask1.getTask().priorityType()).isEqualTo(request1.getPriorityType());
        assertThat(createdTask1.getTask().tag()).isEqualTo(request1.getTag());
        auth.register().login();
        var request2 = createTask();
        var createdTask2 = taskClient.create(request2);
        assertThat(createdTask2.getTask().id()).isEqualTo(request2.getId());
        assertThat(createdTask2.getTask().description()).isEqualTo(request2.getDescription());
        assertThat(createdTask2.getTask().finishDate()).isEqualTo(request2.getFinishDate());
        assertThat(createdTask2.getTask().priorityType()).isEqualTo(request2.getPriorityType());
        assertThat(createdTask2.getTask().tag()).isEqualTo(request2.getTag());

        var responseUser = taskClient.get(request1.getFinishDate().toString(), "0", "200");

        auth.registerAdmin().login();
        var responseAdmin = taskClient.get(request1.getFinishDate().toString(), "0", "200");

        assertThat(responseUser.getTasks().size()).isLessThan(responseAdmin.getTasks().size());
    }

    @Test
    void GET_ALL_TASK_should_returnCorrectData() {
        auth.register().login();
        var requestCommon = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.COMMON.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());
        var createdCommonTask = taskClient.create(requestCommon);
        assertThat(createdCommonTask.getTask().id()).isEqualTo(requestCommon.getId());
        assertThat(createdCommonTask.getTask().description()).isEqualTo(requestCommon.getDescription());
        assertThat(createdCommonTask.getTask().finishDate()).isEqualTo(requestCommon.getFinishDate());
        assertThat(createdCommonTask.getTask().priorityType()).isEqualTo(requestCommon.getPriorityType());
        assertThat(createdCommonTask.getTask().tag()).isEqualTo(requestCommon.getTag());

        var requestUrgent = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.URGENT.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());
        var createdUrgentTask = taskClient.create(requestUrgent);
        assertThat(createdUrgentTask.getTask().id()).isEqualTo(requestUrgent.getId());
        assertThat(createdUrgentTask.getTask().description()).isEqualTo(requestUrgent.getDescription());
        assertThat(createdUrgentTask.getTask().finishDate()).isEqualTo(requestUrgent.getFinishDate());
        assertThat(createdUrgentTask.getTask().priorityType()).isEqualTo(requestUrgent.getPriorityType());
        assertThat(createdUrgentTask.getTask().tag()).isEqualTo(requestUrgent.getTag());

        var requestImportant = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.IMPORTANT.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());
        var createdImportantTask = taskClient.create(requestImportant);
        assertThat(createdImportantTask.getTask().id()).isEqualTo(requestImportant.getId());
        assertThat(createdImportantTask.getTask().description()).isEqualTo(requestImportant.getDescription());
        assertThat(createdImportantTask.getTask().finishDate()).isEqualTo(requestImportant.getFinishDate());
        assertThat(createdImportantTask.getTask().priorityType()).isEqualTo(requestImportant.getPriorityType());
        assertThat(createdImportantTask.getTask().tag()).isEqualTo(requestImportant.getTag());

        var response = taskClient.get("0", "3").getTasks();
        assertThat(response.get(PriorityType.COMMON)).isNotEmpty();
        assertThat(response.get(PriorityType.IMPORTANT)).isNotEmpty();
        assertThat(response.get(PriorityType.URGENT)).isNotEmpty();
    }

    @Test
    void GET_ALL_TASK_should_returnMoreTasksWhenLoginAsAdmin() {
        auth.register().login();
        var requestCommon1 = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.COMMON.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());
        var createdCommonTask1 = taskClient.create(requestCommon1);
        assertThat(createdCommonTask1.getTask().id()).isEqualTo(requestCommon1.getId());
        assertThat(createdCommonTask1.getTask().description()).isEqualTo(requestCommon1.getDescription());
        assertThat(createdCommonTask1.getTask().finishDate()).isEqualTo(requestCommon1.getFinishDate());
        assertThat(createdCommonTask1.getTask().priorityType()).isEqualTo(requestCommon1.getPriorityType());
        assertThat(createdCommonTask1.getTask().tag()).isEqualTo(requestCommon1.getTag());

        var requestUrgent1 = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.URGENT.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());
        var createdUrgentTask1 = taskClient.create(requestUrgent1);
        assertThat(createdUrgentTask1.getTask().id()).isEqualTo(requestUrgent1.getId());
        assertThat(createdUrgentTask1.getTask().description()).isEqualTo(requestUrgent1.getDescription());
        assertThat(createdUrgentTask1.getTask().finishDate()).isEqualTo(requestUrgent1.getFinishDate());
        assertThat(createdUrgentTask1.getTask().priorityType()).isEqualTo(requestUrgent1.getPriorityType());
        assertThat(createdUrgentTask1.getTask().tag()).isEqualTo(requestUrgent1.getTag());

        var requestImportant1 = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.IMPORTANT.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());
        var createdImportantTask1 = taskClient.create(requestImportant1);
        assertThat(createdImportantTask1.getTask().id()).isEqualTo(requestImportant1.getId());
        assertThat(createdImportantTask1.getTask().description()).isEqualTo(requestImportant1.getDescription());
        assertThat(createdImportantTask1.getTask().finishDate()).isEqualTo(requestImportant1.getFinishDate());
        assertThat(createdImportantTask1.getTask().priorityType()).isEqualTo(requestImportant1.getPriorityType());
        assertThat(createdImportantTask1.getTask().tag()).isEqualTo(requestImportant1.getTag());

        auth.register().login();
        var requestCommon2 = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.COMMON.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());
        var createdCommonTask2 = taskClient.create(requestCommon2);
        assertThat(createdCommonTask2.getTask().id()).isEqualTo(requestCommon2.getId());
        assertThat(createdCommonTask2.getTask().description()).isEqualTo(requestCommon2.getDescription());
        assertThat(createdCommonTask2.getTask().finishDate()).isEqualTo(requestCommon2.getFinishDate());
        assertThat(createdCommonTask2.getTask().priorityType()).isEqualTo(requestCommon2.getPriorityType());
        assertThat(createdCommonTask2.getTask().tag()).isEqualTo(requestCommon2.getTag());

        var requestUrgent2 = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.URGENT.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());
        var createdUrgentTask2 = taskClient.create(requestUrgent2);
        assertThat(createdUrgentTask2.getTask().id()).isEqualTo(requestUrgent2.getId());
        assertThat(createdUrgentTask2.getTask().description()).isEqualTo(requestUrgent2.getDescription());
        assertThat(createdUrgentTask2.getTask().finishDate()).isEqualTo(requestUrgent2.getFinishDate());
        assertThat(createdUrgentTask2.getTask().priorityType()).isEqualTo(requestUrgent2.getPriorityType());
        assertThat(createdUrgentTask2.getTask().tag()).isEqualTo(requestUrgent2.getTag());

        var requestImportant2 = new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.IMPORTANT.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString());
        var createdImportantTask2 = taskClient.create(requestImportant2);
        assertThat(createdImportantTask2.getTask().id()).isEqualTo(requestImportant2.getId());
        assertThat(createdImportantTask2.getTask().description()).isEqualTo(requestImportant2.getDescription());
        assertThat(createdImportantTask2.getTask().finishDate()).isEqualTo(requestImportant2.getFinishDate());
        assertThat(createdImportantTask2.getTask().priorityType()).isEqualTo(requestImportant2.getPriorityType());
        assertThat(createdImportantTask2.getTask().tag()).isEqualTo(requestImportant2.getTag());

        var responseUser = taskClient.get("0", "200").getTasks();
        assertThat(responseUser.get(PriorityType.COMMON)).isNotEmpty();
        assertThat(responseUser.get(PriorityType.IMPORTANT)).isNotEmpty();
        assertThat(responseUser.get(PriorityType.URGENT)).isNotEmpty();

        auth.registerAdmin().login();
        var responseAdmin = taskClient.get("0", "200").getTasks();
        assertThat(responseAdmin.get(PriorityType.COMMON)).isNotEmpty();
        assertThat(responseAdmin.get(PriorityType.IMPORTANT)).isNotEmpty();
        assertThat(responseAdmin.get(PriorityType.URGENT)).isNotEmpty();

        assertThat(responseUser.get(PriorityType.COMMON).size()).isLessThan(responseAdmin.get(PriorityType.COMMON).size());
        assertThat(responseUser.get(PriorityType.IMPORTANT).size()).isLessThan(responseAdmin.get(PriorityType.IMPORTANT).size());
        assertThat(responseUser.get(PriorityType.URGENT).size()).isLessThan(responseAdmin.get(PriorityType.URGENT).size());
    }

    private TaskCreateRequest createTask() {
        return new TaskCreateRequest(
                null, UUID.randomUUID().toString(),
                PriorityType.COMMON.name(), UUID.randomUUID().toString(),
                LocalDate.now(), UUID.randomUUID().toString()
        );
    }

    private TaskUpdateRequest updateTask() {
        return new TaskUpdateRequest(null, UPDATED_PRIORITY_TYPE,
                UPDATED_DESCRIPTION, UPDATED_FINISH_DATE, UPDATED_TAG);
    }

}
