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
public interface CrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> {

    default Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    Mapper<E, D> getEntityMapper();

    CrudService<E, ID> getEntityService();

    @GetMapping("/{id}")
    default ResponseEntity<D> getOne(@PathVariable("id") final ID id) {
        return getEntityService().findOne(id)
                .map(optionEntity -> Match(optionEntity).<ResponseEntity<D>>of(
                        Case(Some($()), entity -> ResponseEntity
                                .status(HttpStatus.OK)
                                .body(getEntityMapper().mapTo(entity))),
                        Case(None(), ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(null))))
                .onFailure(e -> getLogger().error("Error while processing GET/{id} request", e))
                .getOrElseGet(e -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null));
    }

    @PostMapping
    default ResponseEntity<D> post(@RequestBody final D dto) {
        return Try
                .of(() -> {
                    final E entity = getEntityMapper().mapFrom(dto);
                    entity.setId(null);
                    return entity;
                })
                .flatMap(entity -> getEntityService().create(entity))
                .map(createdEntity -> getEntityMapper().mapTo(createdEntity))
                .map(createdDto -> ResponseEntity
                        .created(linkTo(getClass()).slash(createdDto.getId()).toUri())
                        .body(createdDto))
                .onFailure(e -> getLogger().error("Error while processing POST request", e))
                .getOrElseGet(e -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null));
    }

    @PutMapping("/{id}")
    default ResponseEntity<D> put(@PathVariable("id") final ID id, @RequestBody final D dto) {
        return Try
                .of(() -> {
                    final E entity = getEntityMapper().mapFrom(dto);
                    entity.setId(id);
                    return entity;
                })
                .flatMap(entity -> getEntityService().exists(entity.getId())
                        .flatMap(exists -> {
                            final Try<E> tryPersistedEntity = exists
                                    ? getEntityService().update(entity)
                                    : getEntityService().create(entity);

                            return tryPersistedEntity
                                    .map(persistedEntity -> getEntityMapper().mapTo(persistedEntity))
                                    .map(persistedDto -> exists
                                            ? ResponseEntity.status(HttpStatus.OK).body(persistedDto)
                                            : ResponseEntity.status(HttpStatus.CREATED).body(persistedDto));
                        }))
                .onFailure(e -> getLogger().error("Error while processing PUT request", e))
                .getOrElseGet(e -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null));
    }

    @DeleteMapping("/{id}")
    default ResponseEntity<Void> delete(@PathVariable("id") final ID id) {
        return getEntityService().delete(id)
                .map(optionEntity -> ResponseEntity
                        .noContent()
                        .build())
                .onFailure(e -> getLogger().error("Error while processing DELETE request", e))
                .getOrElseGet(e -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }
}
