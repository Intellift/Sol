package org.intellift.sol.controller.api;

import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.exception.NotFoundException;
import org.intellift.sol.mapper.Mapper;
import org.intellift.sol.service.CrudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.net.URI;
import java.util.function.Function;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface CrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> {

    Class<E> getEntityClass();

    Mapper<E, D> getEntityMapper();

    CrudService<E, ID> getEntityService();

    @GetMapping("/{id}")
    default ResponseEntity<D> getOne(@PathVariable("id") final ID id) {
        final E entity = getEntityService().findOne(id)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new)
                .getOrElseThrow(() -> new NotFoundException(getEntityClass(), "id", id));

        final D dto = getEntityMapper().mapTo(entity);

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    default ResponseEntity<D> post(@RequestBody final D dto) {
        final E entity = getEntityMapper().mapFrom(dto);

        final E createdEntity = getEntityService().save(entity)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);

        final URI location = linkTo(getClass()).slash(createdEntity.getId()).toUri();

        final D createdDto = getEntityMapper().mapTo(createdEntity);

        return ResponseEntity
                .created(location)
                .body(createdDto);
    }

    @PutMapping("/{id}")
    default ResponseEntity<D> put(@PathVariable("id") final ID id, @RequestBody final D dto) {
        final E entity = getEntityMapper().mapFrom(dto);

        final E replacedEntity = getEntityService().replace(id, entity)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);

        final D replacedDto = getEntityMapper().mapTo(replacedEntity);

        return ResponseEntity.ok(replacedDto);
    }

    @DeleteMapping("/{id}")
    default ResponseEntity<Void> delete(@PathVariable("id") final ID id) {
        getEntityService().delete(id);

        return ResponseEntity.noContent().build();
    }
}
