package org.intellift.sol.service.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.querydsl.repository.QueryDslRepository;
import org.intellift.sol.service.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

public interface QueryDslCrudService<E extends Identifiable<ID>, ID extends Serializable> extends CrudService<E, ID> {

    @Override
    QueryDslRepository<E, ID> getRepository();

    default Try<Boolean> exists(final Predicate predicate) {
        Objects.requireNonNull(predicate, "predicate is null");

        return Try.of(() -> getRepository().exists(predicate));
    }

    default Try<Long> count(final Predicate predicate) {
        Objects.requireNonNull(predicate, "predicate is null");

        return Try.of(() -> getRepository().count(predicate));
    }

    default Try<Option<E>> findOne(final Predicate predicate) {
        return Try.of(() -> Option.ofOptional(getRepository().findOne(predicate)));
    }

    default Try<E> findOne(final Predicate predicate, final Supplier<? extends Exception> ifNotFound) {
        Objects.requireNonNull(ifNotFound, "ifNotFound is null");

        return Try.of(() -> Option.ofOptional(getRepository().findOne(predicate)))
                .flatMap(entityOption -> entityOption.toTry(ifNotFound));
    }

    default Try<Stream<E>> findAll(final Predicate predicate) {
        return Try.of(() -> Stream.ofAll(getRepository().findAll(predicate)));
    }

    default Try<Stream<E>> findAll(final Predicate predicate, final Sort sort) {
        Objects.requireNonNull(sort, "sort is null");

        return Try.of(() -> Stream.ofAll(getRepository().findAll(predicate, sort)));
    }

    default Try<Stream<E>> findAll(final Predicate predicate, final OrderSpecifier<?>... orders) {
        return Try.of(() -> Stream.ofAll(getRepository().findAll(predicate, orders)));
    }

    default Try<Stream<E>> findAll(final OrderSpecifier<?>... orders) {
        return Try.of(() -> Stream.ofAll(getRepository().findAll(orders)));
    }

    default Try<Page<E>> findAll(final Predicate predicate, final Pageable pageable) {
        return Try.of(() -> getRepository().findAll(predicate, pageable));
    }
}
