package io.modicon.taskapp.domain.repository;

import io.modicon.taskapp.domain.model.TagEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.modicon.taskapp.infrastructure.exception.ApiException.exception;

public interface TagDataSource {

    interface Read {

        List<TagEntity> findTagWithTasks(Long taskCount);

        TagEntity findByName(String name);

        void validateNotExist(String name);

        TagEntity supplyTag(String name);

        Optional<TagEntity> tryToFindTag(String name);
    }

    interface Write {

        void save(TagEntity tag);

        void delete(TagEntity tag);
    }

    @Transactional(readOnly = true)
    @RequiredArgsConstructor
    @Service
    class JpaReadTagDataSource implements TagDataSource.Read {

        private final JpaTagRepository jpaTagRepository;

        @Override
        public List<TagEntity> findTagWithTasks(Long taskCount) {
            return jpaTagRepository.findAllByTaskCountIsGreaterThan(taskCount);
        }

        @Override
        public TagEntity findByName(String name) {
            return jpaTagRepository.findByTagName(name)
                    .orElseThrow(() -> exception(HttpStatus.NOT_FOUND, "tag [%s] not found", name));
        }

        @Override
        public void validateNotExist(String name) {
            if (jpaTagRepository.existsByTagName(name))
                throw exception(HttpStatus.BAD_REQUEST, "tag [%s] already exist", name);
        }

        @Override
        public TagEntity supplyTag(String name) {
            return jpaTagRepository.findByTagName(name).orElseGet(() -> new TagEntity(UUID.randomUUID().toString(), name, 0L));
        }

        @Override
        public Optional<TagEntity> tryToFindTag(String name) {
            return jpaTagRepository.findByTagName(name);
        }
    }

    @Transactional
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
