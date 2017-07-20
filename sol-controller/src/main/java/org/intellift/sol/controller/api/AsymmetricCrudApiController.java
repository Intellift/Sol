package org.intellift.sol.controller.api;

import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.mapper.Mapper;
import org.intellift.sol.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

import static javaslang.API.*;
import static javaslang.Patterns.None;
import static javaslang.Patterns.Some;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface AsymmetricCrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, RD extends Identifiable<ID>, ID extends Serializable> {

    default Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    Mapper<E, D> getMapper();

    Mapper<E, RD> getReferenceMapper();

    CrudService<E, ID> getService();

    @GetMapping("/{id}")
    default ResponseEntity<D> getOne(@PathVariable("id") final ID id) {
        return getService().findOne(id)
                .map(optionalEntity -> Match(optionalEntity).<ResponseEntity<D>>of(

                        Case(Some($()), entity -> ResponseEntity
                                .status(HttpStatus.OK)
                                .body(getMapper().mapTo(entity))),

                        Case(None(), ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .build())))
                .onFailure(throwable -> getLogger().error("Error occurred while processing GET/{id} request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @PostMapping
    default ResponseEntity<D> post(@RequestBody final RD dto) {
        return Try
                .of(() -> {
                    final E entity = getReferenceMapper().mapFrom(dto);
                    entity.setId(null);
                    return entity;
                })
                .flatMap(entity -> getService().create(entity))
                .map(createdEntity -> getMapper().mapTo(createdEntity))
                .map(createdDto -> ResponseEntity
                        .created(linkTo(getClass()).slash(createdDto.getId()).toUri())
                        .body(createdDto))
                .onFailure(throwable -> getLogger().error("Error occurred while processing POST request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @PutMapping("/{id}")
    default ResponseEntity<D> put(@PathVariable("id") final ID id, @RequestBody final RD dto) {
        return Try
                .of(() -> {
                    final E entity = getReferenceMapper().mapFrom(dto);
                    entity.setId(id);
                    return entity;
                })
                .flatMap(entity -> getService().exists(entity.getId())
                        .flatMap(exists -> exists

                                ? getService().update(entity)
                                .map(persistedEntity -> getMapper().mapTo(persistedEntity))
                                .map(persistedDto -> ResponseEntity
                                        .status(HttpStatus.OK)
                                        .body(persistedDto))

                                : getService().create(entity)
                                .map(persistedEntity -> getMapper().mapTo(persistedEntity))
                                .map(persistedDto -> ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(persistedDto))))
                .onFailure(throwable -> getLogger().error("Error occurred while processing PUT request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @DeleteMapping("/{id}")
    default ResponseEntity<Void> delete(@PathVariable("id") final ID id) {
        return getService().delete(id)
                .map(ignored -> ResponseEntity
                        .noContent()
                        .<Void>build())
                .onFailure(throwable -> getLogger().error("Error occurred while processing DELETE request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }
}
