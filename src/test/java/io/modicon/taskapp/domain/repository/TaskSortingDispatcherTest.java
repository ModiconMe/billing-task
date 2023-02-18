package io.modicon.taskapp.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class TaskSortingDispatcherTest {

    private TaskSortingDispatcher taskSortingDispatcher;

    @BeforeEach
    void setUp() {
        taskSortingDispatcher = new TaskSortingDispatcher.PriorityTypeSortDispatcher();
    }

    @Test
    void getPage() {
        String page = "0";
        String limit = "1";
        Pageable actual = taskSortingDispatcher.getPage(page, limit);

        PageRequest expected = PageRequest.of(Integer.parseInt(page), Integer.parseInt(limit), Sort.by("priorityType"));

        assertEquals(expected, actual);
    }
}