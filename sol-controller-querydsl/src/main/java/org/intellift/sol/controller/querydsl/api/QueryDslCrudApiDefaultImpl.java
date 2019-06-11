package org.intellift.sol.controller.querydsl.api;

import com.querydsl.core.types.Predicate;
import io.vavr.Function2;
import io.vavr.control.Try;
import org.intellift.sol.controller.api.CrudApiDefaultImpl;
import org.intellift.sol.domain.Identifiable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class QueryDslCrudApiDefaultImpl extends CrudApiDefaultImpl {

    public static <E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable>
    Function2<Predicate, Pageable, Try<ResponseEntity<Page<D>>>> getAll(final BiFunction<Predicate, Pageable, Try<Page<E>>> findAll,
                                                                        final Function<Page<E>, Page<D>> toDTO) {
        return (final Predicate predicate, final Pageable pageable) -> findAll.apply(predicate, pageable)
                .map(toDTO)
                .map(ResponseEntity::ok);
    }
}
