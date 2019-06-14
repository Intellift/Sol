package org.intellift.sol.controller.simple.api;

import io.vavr.control.Try;
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

public interface SimpleAsymmetricCrudApiController<E extends Identifiable<ID>, DD extends Identifiable<ID>, SD extends Identifiable<ID>, ID extends Serializable> extends AsymmetricCrudApiController<E, DD, SD, ID> {

    @Override
    PageMapper<E, DD> getDeepMapper();

    @Override
    PageMapper<E, SD> getShallowMapper();

    @GetMapping
    default ResponseEntity<Page<DD>> getAll(final Pageable pageable) throws Throwable {
        final Function<Pageable, Try<ResponseEntity<Page<DD>>>> getAll = SimpleCrudApiDefaultImpl.getAll(
                getService()::findAll,
                getDeepMapper()::mapTo
        );

        return getAll.apply(pageable)
                .onFailure(throwable -> getLogger().error("Error occurred while processing GET request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }
}
