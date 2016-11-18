package org.intellift.sol.service;


import javaslang.Tuple;
import javaslang.collection.Stream;
import javaslang.control.Option;
import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.exception.NotFoundException;
import org.intellift.sol.domain.repository.Repository;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface CrudService<E extends Identifiable<ID>, ID extends Serializable> {

    Class<E> getEntityClass();

    Repository<E, ID> getEntityRepository();

    default Try<E> save(final E entity) {
        return Try.of(() -> getEntityRepository().save(entity));
    }

    default Try<Stream<E>> findAll() {
        return Try.of(() -> Stream.ofAll(getEntityRepository().findAll()));
    }

    default Try<Option<E>> findOne(final ID id) {
        return Try.of(() -> Option.of(getEntityRepository().findOne(id)));
    }

    default Try<E> replace(final ID id, final E entity) {
        return Try
                .of(() -> Option
                        .when(getEntityRepository().exists(id), () -> Tuple.of(id, entity))
                        .getOrElseThrow(() -> new NotFoundException(getEntityClass(), "id", id)))
                .flatMap(t -> Objects.equals(t._1, t._2.getId()) ? Try.of(() -> Option.of(t._2)) : delete(t._1))
                .flatMap(e -> save(e.get()));
    }

    default Try<Option<E>> delete(final ID id) {
        return findOne(id).flatMap(e ->
                Try.of(() -> {
                    if (e.isDefined()) getEntityRepository().delete(e.get());

                    return e;
                })
        );
    }
}
