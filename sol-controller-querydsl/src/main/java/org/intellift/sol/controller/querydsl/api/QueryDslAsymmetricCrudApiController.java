package org.intellift.sol.controller.querydsl.api;

import com.querydsl.core.types.Predicate;
import io.vavr.control.Try;
import org.intellift.sol.controller.api.AsymmetricCrudApiController;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.mapper.PageMapper;
import org.intellift.sol.service.querydsl.QueryDslCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.Serializable;
import java.util.function.BiFunction;

public interface QueryDslAsymmetricCrudApiController<E extends Identifiable<ID>, DD extends Identifiable<ID>, SD extends Identifiable<ID>, ID extends Serializable> extends AsymmetricCrudApiController<E, DD, SD, ID> {

    @Override
    QueryDslCrudService<E, ID> getService();

    @Override
    PageMapper<E, DD> getDeepMapper();

    @Override
    PageMapper<E, SD> getShallowMapper();

    @GetMapping
    ResponseEntity<Page<DD>> getAll(Predicate predicate, Pageable pageable) throws Throwable;

    default ResponseEntity<Page<DD>> getAllDefaultImplementation(final Predicate predicate, final Pageable pageable) {
        final BiFunction<Predicate, Pageable, Try<ResponseEntity<Page<DD>>>> getAll = QueryDslCrudApiDefaultImpl.getAll(
                getService()::findAll,
                getDeepMapper()::mapTo
        );

        return getAll.apply(predicate, pageable)
                .onFailure(throwable -> getLogger().error("Error occurred while processing GET request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }
}
