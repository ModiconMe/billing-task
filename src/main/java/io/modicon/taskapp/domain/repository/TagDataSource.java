package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.TagEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TagDataSource {

    interface Read {

        List<TagEntity> findTagWithTasks(Long taskCount);

        TagEntity findById(String id);

        void validateNotExist(String id);
    }

    interface Write {

        void save(TagEntity tag);

        void delete(TagEntity tag);
    }

    @RequiredArgsConstructor
    @Service
    class JpaReadTagDataSource implements TagDataSource.Read {

        private final JpaTagRepository jpaTagRepository;

        @Override
        public List<TagEntity> findTagWithTasks(Long taskCount) {
            return jpaTagRepository.findAllByTaskCountIsGreaterThan(taskCount);
        }

        @Override
        public TagEntity findById(String id) {
            return jpaTagRepository.findById(id)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "tag [%s] not found", id));;
        }

        @Override
        public void validateNotExist(String id) {
            if (jpaTagRepository.existsById(id))
                throw exception(HttpStatus.BAD_REQUEST, "tag [%s] already exist", id);
        }
    }

    @RequiredArgsConstructor
    @Service
    class JpaWriteTagDataSource implements TagDataSource.Write {

        private final JpaTagRepository jpaTagRepository;

        @Override
        public void save(TagEntity tag) {
            jpaTagRepository.save(tag);
        }

        @Override
        public void delete(TagEntity tag) {
            jpaTagRepository.delete(tag);
        }
    }
}
