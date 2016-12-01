package org.intellift.sol.service;


import javaslang.collection.Stream;
import javaslang.control.Option;
import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface CrudService<E extends Identifiable<ID>, ID extends Serializable> {

    Class<E> getEntityClass();

    Repository<E, ID> getEntityRepository();

    default Try<Boolean> exists(final ID id) {
        return Try.of(() -> getEntityRepository().exists(id));
    }

    default Try<E> save(final E entity) {
        return Try.of(() -> getEntityRepository().save(entity));
    }

    default Try<E> create(final E entity) {
        return save(entity);
    }

    default Try<E> update(final E entity) {
        return save(entity);
    }

    default Try<Page<E>> findAll(final Pageable pageable) {
        return Try.of(() -> getEntityRepository().findAll(pageable));
    }

    default Try<Stream<E>> findAll() {
        return Try.of(() -> Stream.ofAll(getEntityRepository().findAll()));
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
                .flatMap(e -> Try
                        .of(() -> {
                            if (e.isDefined()) {
                                getEntityRepository().delete(e.get());
                            }

                            return e;
                        })
                );
    }

    default Try<Option<E>> delete(final E entity) {
        return Try
                .of(entity::getId)
                .flatMap(this::delete);
    }
}
