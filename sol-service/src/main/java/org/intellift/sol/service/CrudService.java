package org.intellift.sol.service;


import javaslang.collection.Iterator;
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

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
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

    default Try<Seq<E>> save(final Iterable<E> entities) {
        Objects.requireNonNull(entities, "entities is null");

        return Iterator.ofAll(entities)
                .map(this::save)
                .transform(Try::sequence);
    }

    default Try<E> create(final E entity) {
        Objects.requireNonNull(entity, "entity is null");

        return save(entity);
    }

    default Try<Seq<E>> create(final Iterable<E> entities) {
        Objects.requireNonNull(entities, "entities is null");

        return save(entities);
    }

    default Try<E> update(final E entity) {
        Objects.requireNonNull(entity, "entity is null");

        return save(entity);
    }

    default Try<Seq<E>> update(final Iterable<E> entities) {
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

    default Try<Seq<E>> findAll(final Iterable<ID> ids) {
        Objects.requireNonNull(ids, "ids is null");

        return Iterator.ofAll(ids)
                .map(this::findOne)
                .transform(Try::sequence)
                .map(sequence -> sequence
                        .filter(Option::isDefined)
                        .map(Option::get));
    }

    default Try<Option<E>> findOne(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return Try.of(() -> Option.of(getRepository().findOne(id)));
    }

    default Try<Option<E>> findOne(final E entity) {
        Objects.requireNonNull(entity, "entity is null");

        return findOne(entity.getId());
    }

    default Try<Void> delete(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return Try.run(() -> getRepository().delete(id));
    }

    default Try<Void> delete(final E entity) {
        Objects.requireNonNull(entity, "entity is null");

        return delete(entity.getId());
    }

    default Try<Void> delete(final Iterable<ID> ids) {
        Objects.requireNonNull(ids, "ids is null");

        return Iterator.ofAll(ids)
                .map(this::delete)
                .transform(Try::sequence)
                .flatMap(ignored -> Try.<Void>success(null));
    }
}
