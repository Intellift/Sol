package org.intellift.sol.controller.simple.api;

import io.vavr.control.Try;
import org.intellift.sol.controller.api.CrudApiController;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.mapper.PageMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.Serializable;
import java.util.function.Function;

public interface SimpleCrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> extends CrudApiController<E, D, ID> {

    @Override
    default PageMapper<E, D> getDeepMapper() {
        return getMapper();
    }

    @Override
    default PageMapper<E, D> getShallowMapper() {
        return getMapper();
    }

    @Override
    PageMapper<E, D> getMapper();

    @GetMapping
    default ResponseEntity<Page<D>> getAll(final Pageable pageable) throws Throwable {
        final Function<Pageable, Try<ResponseEntity<Page<D>>>> getAll = SimpleCrudApiDefaultImpl.getAll(
                getService()::findAll,
                getMapper()::mapTo
        );

        return getAll.apply(pageable)
                .onFailure(throwable -> getLogger().error("Error occurred while processing GET request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }
}
