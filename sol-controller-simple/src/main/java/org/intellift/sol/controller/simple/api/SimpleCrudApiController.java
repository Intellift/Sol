package org.intellift.sol.controller.simple.api;

import org.intellift.sol.controller.api.CrudApiController;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.mapper.PageMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface SimpleCrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> extends CrudApiController<E, D, ID> {

    @Override
    PageMapper<E, D> getEntityMapper();

    @GetMapping
    default ResponseEntity<Page<D>> getAll(final Pageable pageable) {
        return getEntityService().findAll(pageable)
                .map(page -> getEntityMapper().mapTo(page))
                .map(ResponseEntity::ok)
                .onFailure(e -> getLogger().error("", e))
                .getOrElseGet(e -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null));
    }
}
