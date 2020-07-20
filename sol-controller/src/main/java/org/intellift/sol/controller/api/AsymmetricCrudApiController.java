package org.intellift.sol.controller.api;

import io.vavr.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.mapper.Mapper;
import org.intellift.sol.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface AsymmetricCrudApiController<E extends Identifiable<ID>, DD extends Identifiable<ID>, SD extends Identifiable<ID>, ID extends Serializable> {

    default Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    CrudService<E, ID> getService();

    Mapper<E, DD> getDeepMapper();

    Mapper<E, SD> getShallowMapper();

    @GetMapping("/{id}")
    default ResponseEntity<DD> getOne(@PathVariable("id") final ID id) throws Throwable {
        final Function<ID, Try<ResponseEntity<DD>>> getOne = CrudApiDefaultImpl.getOne(
                getService()::findOne,
                getDeepMapper()::mapTo
        );

        return getOne.apply(id)
                .onFailure(throwable -> getLogger().error("Error occurred while processing GET/{id} request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @PostMapping
    default ResponseEntity<DD> post(@RequestBody final SD dto) throws Throwable {
        final Function<ID, URI> constructLocation = persistedId -> WebMvcLinkBuilder.linkTo(getClass()).slash(persistedId).toUri();

        final Function<SD, Try<ResponseEntity<DD>>> asymmetricPost = CrudApiDefaultImpl.asymmetricPost(
                getService()::create,
                getShallowMapper()::mapFrom,
                getDeepMapper()::mapTo,
                constructLocation
        );

        return asymmetricPost.apply(dto)
                .onFailure(throwable -> getLogger().error("Error occurred while processing POST request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @PutMapping("/{id}")
    default ResponseEntity<DD> put(@PathVariable("id") final ID id, @RequestBody final SD dto) throws Throwable {
        final Function<ID, URI> constructLocation = persistedId -> WebMvcLinkBuilder.linkTo(getClass()).slash(persistedId).toUri();

        final BiFunction<ID, SD, Try<ResponseEntity<DD>>> asymmetricPut = CrudApiDefaultImpl.asymmetricPut(
                getService()::exists,
                getService()::create,
                getService()::update,
                getShallowMapper()::mapFrom,
                getDeepMapper()::mapTo,
                constructLocation
        );

        return asymmetricPut.apply(id, dto)
                .onFailure(throwable -> getLogger().error("Error occurred while processing PUT request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @DeleteMapping("/{id}")
    default ResponseEntity<Void> delete(@PathVariable("id") final ID id) throws Throwable {
        final Function<ID, Try<ResponseEntity<Void>>> delete = CrudApiDefaultImpl.delete(getService()::delete);

        return delete.apply(id)
                .onFailure(throwable -> getLogger().error("Error occurred while processing DELETE request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }
}
