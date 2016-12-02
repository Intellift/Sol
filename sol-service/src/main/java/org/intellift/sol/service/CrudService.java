package org.intellift.sol.service;


import javaslang.collection.Stream;
import javaslang.control.Option;
import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface CrudService<E extends Identifiable<ID>, ID extends Serializable> {

    Repository<E, ID> getEntityRepository();

    default Try<Boolean> exists(final ID id) {
        return Try.of(() -> getEntityRepository().exists(id));
    }

    default Try<E> save(final E entity) {
        return Try.of(() -> getEntityRepository().save(entity));
    }

    default Try<Stream<E>> save(final Iterable<E> entities) {
        return Try.of(() -> entities.iterator().hasNext()
                ? Stream.ofAll(getEntityRepository().save(entities))
                : Stream.empty());
    }

    default Try<E> create(final E entity) {
        return save(entity);
    }

    default Try<Stream<E>> create(final Iterable<E> entities) {
        return save(entities);
    }

    default Try<E> update(final E entity) {
        return save(entity);
    }

    default Try<Stream<E>> update(final Iterable<E> entities) {
        return save(entities);
    }

    default Try<Stream<E>> findAll(final Sort sort) {
        return Try.of(() -> Stream.ofAll(getEntityRepository().findAll(sort)));
    }

    default Try<Page<E>> findAll(final Pageable pageable) {
        return Try.of(() -> getEntityRepository().findAll(pageable));
    }

    default Try<Stream<E>> findAll() {
        return Try.of(() -> Stream.ofAll(getEntityRepository().findAll()));
    }

    default Try<Stream<E>> findAll(final Iterable<ID> ids) {
        return Try.of(() -> Stream.ofAll(getEntityRepository().findAll(ids)));
    }

    default Try<Option<E>> findOne(final ID id) {
        return Try.of(() -> Option.of(getEntityRepository().findOne(id)));
    }

    default Try<Option<E>> findOne(final E entity) {
        return Try
                .of(entity::getId)
                .flatMap(this::findOne);
    }

    default Try<Option<E>> delete(final ID id) {
        return findOne(id)
                .map(entity -> entity
                        .peek(e -> getEntityRepository().delete(e)));
    }

    default Try<Option<E>> delete(final E entity) {
        return Try
                .of(entity::getId)
                .flatMap(this::delete);
    }
}
