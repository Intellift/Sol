package org.intellift.sol.service;


import javaslang.collection.List;
import javaslang.collection.Stream;
import javaslang.control.Option;
import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface CrudService<E extends Identifiable<ID>, ID extends Serializable> {

    Repository<E, ID> getEntityRepository();

    default Try<Boolean> exists(final ID id) {
        return Try
                .of(() -> requireNonNull(id, "Id to check if exists must not be null"))
                .map(notNullId -> getEntityRepository().exists(notNullId));
    }

    default Try<Long> count() {
        return Try.of(() -> getEntityRepository().count());
    }

    default Try<E> save(final E entity) {
        return Try
                .of(() -> requireNonNull(entity, "Entity to save must not be null"))
                .map(notNullEntity -> getEntityRepository().save(notNullEntity));
    }

    default Try<List<E>> save(final Iterable<E> entities) {
        return Try
                .of(() -> requireNonNull(entities, "Entities to save must not be null"))
                .map(iterable -> iterable.iterator().hasNext()
                        ? List.ofAll(getEntityRepository().save(iterable))
                        : List.empty());
    }

    default Try<E> create(final E entity) {
        return save(entity);
    }

    default Try<List<E>> create(final Iterable<E> entities) {
        return save(entities);
    }

    default Try<E> update(final E entity) {
        return save(entity);
    }

    default Try<List<E>> update(final Iterable<E> entities) {
        return save(entities);
    }

    default Try<Stream<E>> findAll(final Sort sort) {
        return Try
                .of(() -> requireNonNull(sort, "Sort must not be null"))
                .map(notNullSort -> Stream.ofAll(getEntityRepository().findAll(notNullSort)));
    }

    default Try<Page<E>> findAll(final Pageable pageable) {
        return Try
                .of(() -> requireNonNull(pageable, "Pageable must not be null"))
                .map(notNullPageable -> getEntityRepository().findAll(notNullPageable));
    }

    default Try<Stream<E>> findAll() {
        return Try.of(() -> Stream.ofAll(getEntityRepository().findAll()));
    }

    default Try<List<E>> findAll(final Iterable<ID> ids) {
        return Try
                .of(() -> requireNonNull(ids, "Ids to find must not be null"))
                .map(iterable -> List.ofAll(getEntityRepository().findAll(iterable)));
    }

    default Try<Option<E>> findOne(final ID id) {
        return Try
                .of(() -> requireNonNull(id, "Id to find must not be null"))
                .map(notNullId -> Option.of(getEntityRepository().findOne(notNullId)));
    }

    default Try<Option<E>> findOne(final E entity) {
        return Try
                .of(() -> requireNonNull(entity, "Entity to find must not be null"))
                .flatMap(this::findOne);
    }

    default Try<Option<E>> delete(final ID id) {
        return Try
                .of(() -> requireNonNull(id, "Id to delete must not be null"))
                .flatMap(this::findOne)
                .peek(entity -> entity
                        .peek(e -> getEntityRepository().delete(e)));
    }

    default Try<Option<E>> delete(final E entity) {
        return Try
                .of(() -> requireNonNull(entity, "Entity to delete must not be null"))
                .map(Identifiable::getId)
                .flatMap(this::delete);
    }

    default Try<List<E>> delete(final Iterable<ID> ids) {
        return Try
                .of(() -> requireNonNull(ids, "Ids to delete must not be null"))
                .flatMap(this::findAll)
                .peek(entities -> {
                    if (entities.nonEmpty()) {
                        getEntityRepository().delete(entities);
                    }
                });
    }
}
