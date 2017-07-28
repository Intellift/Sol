package org.intellift.sol.controller.simple.api;

import javaslang.control.Try;
import org.intellift.sol.controller.api.AsymmetricCrudApiController;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.mapper.PageMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.Serializable;
import java.util.function.Function;

public interface SimpleAsymmetricCrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, RD extends Identifiable<ID>, ID extends Serializable> extends AsymmetricCrudApiController<E, D, RD, ID> {

    @Override
    PageMapper<E, D> getMapper();

    @Override
    PageMapper<E, RD> getReferenceMapper();

    @GetMapping
    default ResponseEntity<Page<D>> getAll(final Pageable pageable) {
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
