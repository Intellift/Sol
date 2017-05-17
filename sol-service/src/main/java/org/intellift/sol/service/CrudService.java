package org.intellift.sol.service;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.Objects;

import static io.vavr.API.Option;
import static io.vavr.API.Try;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface CrudService<E extends Identifiable<ID>, ID extends Serializable> {

    Repository<E, ID> getRepository();

    default Try<Boolean> exists(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return Try(() -> getRepository().exists(id));
    }

    default Try<Long> count() {
        return Try(() -> getRepository().count());
    }

    default Try<E> save(final E entity) {
        Objects.requireNonNull(entity, "entity is null");

        return Try(() -> getRepository().save(entity));
    }

    default Try<List<E>> save(final Seq<E> entities) {
        Objects.requireNonNull(entities, "entities is null");

        return Try(() -> List.ofAll(getRepository().save(entities.asJava())));
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

        return Try(() -> Stream.ofAll(getRepository().findAll(sort)));
    }

    default Try<Page<E>> findAll(final Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable is null");

        return Try(() -> getRepository().findAll(pageable));
    }

    default Try<Stream<E>> findAll() {
        return Try(() -> Stream.ofAll(getRepository().findAll()));
    }

    default Try<List<E>> findAll(final Seq<ID> ids) {
        Objects.requireNonNull(ids, "ids is null");

        return Try(() -> List.ofAll(getRepository().findAll(ids.asJava())));
    }

    default Try<Option<E>> findOne(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return Try(() -> Option(getRepository().findOne(id)));
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

    default Try<Void> delete(final Seq<E> entities) {
        Objects.requireNonNull(entities, "entities is null");

        return Try.run(() -> getRepository().delete(entities.asJava()));
    }
}
