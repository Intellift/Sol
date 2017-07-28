package org.intellift.sol.controller.api;

import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.mapper.Mapper;
import org.intellift.sol.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface AsymmetricCrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, RD extends Identifiable<ID>, ID extends Serializable> {

    default Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    Mapper<E, D> getMapper();

    Mapper<E, RD> getReferenceMapper();

    CrudService<E, ID> getService();

    @GetMapping("/{id}")
    default ResponseEntity<D> getOne(@PathVariable("id") final ID id) {
        final Function<ID, Try<ResponseEntity<D>>> getOne = CrudApiDefaultImpl.getOne(
                getService()::findOne,
                getMapper()::mapTo
        );

        return getOne.apply(id)
                .onFailure(throwable -> getLogger().error("Error occurred while processing GET/{id} request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @PostMapping
    default ResponseEntity<D> post(@RequestBody final RD dto) {
        final Function<ID, URI> constructLocation = persistedId -> ControllerLinkBuilder.linkTo(getClass()).slash(persistedId).toUri();

        final Function<RD, Try<ResponseEntity<D>>> asymmetricPost = CrudApiDefaultImpl.asymmetricPost(
                getService()::create,
                getReferenceMapper()::mapFrom,
                getMapper()::mapTo,
                constructLocation
        );

        return asymmetricPost.apply(dto)
                .onFailure(throwable -> getLogger().error("Error occurred while processing POST request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @PutMapping("/{id}")
    default ResponseEntity<D> put(@PathVariable("id") final ID id, @RequestBody final RD dto) {
        final Function<ID, URI> constructLocation = persistedId -> ControllerLinkBuilder.linkTo(getClass()).slash(persistedId).toUri();

        final BiFunction<ID, RD, Try<ResponseEntity<D>>> asymmetricPut = CrudApiDefaultImpl.asymmetricPut(
                getService()::exists,
                getService()::create,
                getService()::update,
                getReferenceMapper()::mapFrom,
                getMapper()::mapTo,
                constructLocation
        );

        return asymmetricPut.apply(id, dto)
                .onFailure(throwable -> getLogger().error("Error occurred while processing PUT request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @DeleteMapping("/{id}")
    default ResponseEntity<Void> delete(@PathVariable("id") final ID id) {
        final Function<ID, Try<ResponseEntity<Void>>> delete = CrudApiDefaultImpl.delete(getService()::delete);

        return delete.apply(id)
                .onFailure(throwable -> getLogger().error("Error occurred while processing DELETE request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }
}
