package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.PriorityType;
import io.modicon.taskapp.domain.model.TaskEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public interface TaskSortingDispatcher {

    Pageable getPage(String page, String limit);

    @Service
    class PriorityTypeSortDispatcher implements TaskSortingDispatcher {

        @Override
        public Pageable getPage(String page, String limit) {
            Optional<Field> fieldToSort = Arrays
                    .stream(TaskEntity.class.getDeclaredFields())
                    .filter(f -> f.getType().equals(PriorityType.class))
                    .findFirst();

            return fieldToSort.map(f -> PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit),
                            Sort.by(fieldToSort.get().getName())))
                    .orElseGet(() -> PageRequest.of(
                            Integer.parseInt(page),
                            Integer.parseInt(limit)));
        }
    }
}
