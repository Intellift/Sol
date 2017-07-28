package org.intellift.sol.controller.api;

import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.mapper.Mapper;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface CrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> extends AsymmetricCrudApiController<E, D, D, ID> {

    @Override
    default Mapper<E, D> getReferenceMapper() {
        return getMapper();
    }

    @Override
    @PostMapping
    default ResponseEntity<D> post(@RequestBody final D dto) {
        final Function<ID, URI> constructLocation = persistedId -> ControllerLinkBuilder.linkTo(getClass()).slash(persistedId).toUri();

        final Function<D, Try<ResponseEntity<D>>> post = CrudApiDefaultImpl.post(
                getService()::create,
                getMapper()::mapFrom,
                getMapper()::mapTo,
                constructLocation
        );

        return post.apply(dto)
                .onFailure(throwable -> getLogger().error("Error occurred while processing POST request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @Override
    @PutMapping("/{id}")
    default ResponseEntity<D> put(@PathVariable("id") final ID id, @RequestBody final D dto) {
        final Function<ID, URI> constructLocation = persistedId -> ControllerLinkBuilder.linkTo(getClass()).slash(persistedId).toUri();

        final BiFunction<ID, D, Try<ResponseEntity<D>>> put = CrudApiDefaultImpl.put(
                getService()::exists,
                getService()::create,
                getService()::update,
                getMapper()::mapFrom,
                getMapper()::mapTo,
                constructLocation
        );

        return put.apply(id, dto)
                .onFailure(throwable -> getLogger().error("Error occurred while processing PUT request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }
}
