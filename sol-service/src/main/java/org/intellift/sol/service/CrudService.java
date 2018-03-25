package org.intellift.sol.service;

import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.collection.Stream;
import javaslang.control.Option;
import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

public interface CrudService<E extends Identifiable<ID>, ID extends Serializable> {

    Repository<E, ID> getRepository();

    default Try<Boolean> exists(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return Try.of(() -> getRepository().exists(id));
    }

    default Try<Long> count() {
        return Try.of(() -> getRepository().count());
    }

    default Try<E> save(final E entity) {
        Objects.requireNonNull(entity, "entity is null");

        return Try.of(() -> getRepository().save(entity));
    }

    default Try<List<E>> save(final Seq<E> entities) {
        Objects.requireNonNull(entities, "entities is null");

        return Try.of(() -> List.ofAll(getRepository().save(entities.toJavaList())));
    }

    default Try<E> create(final E entity) {
        Objects.requireNonNull(entity, "entity is null");

        return save(entity);
    }

    default Try<List<E>> create(final Seq<E> entities) {
        Objects.requireNonNull(entities, "entities is null");

        return save(entities);
    }

    default Try<E> update(final E entity) {
        Objects.requireNonNull(entity, "entity is null");

        return save(entity);
    }

    default Try<List<E>> update(final Seq<E> entities) {
        Objects.requireNonNull(entities, "entities is null");

        return save(entities);
    }

    default Try<Stream<E>> findAll(final Sort sort) {
        Objects.requireNonNull(sort, "sort is null");

        return Try.of(() -> Stream.ofAll(getRepository().findAll(sort)));
    }

    default Try<Page<E>> findAll(final Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable is null");

        return Try.of(() -> getRepository().findAll(pageable));
    }

    default Try<Stream<E>> findAll() {
        return Try.of(() -> Stream.ofAll(getRepository().findAll()));
    }

    default Try<List<E>> findAll(final Seq<ID> ids) {
        Objects.requireNonNull(ids, "ids is null");

        return Try.of(() -> List.ofAll(getRepository().findAll(ids.toJavaList())));
    }

    default Try<Option<E>> findOne(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return Try.of(() -> Option.of(getRepository().findOne(id)));
    }

    default Try<Option<E>> findOne(final E entity) {
        Objects.requireNonNull(entity, "entity is null");

        return findOne(entity.getId());
    }

    default Try<E> findOne(final ID id, final Supplier<? extends Exception> ifNotFound) {
        Objects.requireNonNull(id, "id is null");
        Objects.requireNonNull(ifNotFound, "ifNotFound is null");

        return Try.of(() -> Option.of(getRepository().findOne(id)))
                .flatMap(entityOption -> entityOption.toTry(ifNotFound));
    }

    default Try<E> findOne(final E entity, final Supplier<? extends Exception> ifNotFound) {
        Objects.requireNonNull(entity, "entity is null");
        Objects.requireNonNull(ifNotFound, "ifNotFound is null");

        return findOne(entity.getId(), ifNotFound);
    }

    default Try<Void> delete(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return Try.run(() -> getRepository().delete(id));
    }

    default Try<Void> delete(final E entity) {
        Objects.requireNonNull(entity, "entity is null");

        return Try.run(() -> getRepository().delete(entity));
    }

    default Try<Void> delete(final Seq<E> entities) {
        Objects.requireNonNull(entities, "entities is null");

        return Try.run(() -> getRepository().delete(entities.toJavaList()));
    }
}
