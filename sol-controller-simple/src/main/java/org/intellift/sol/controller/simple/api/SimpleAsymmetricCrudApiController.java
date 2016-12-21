package org.intellift.sol.controller.simple.api;

import org.intellift.sol.controller.api.AsymmetricCrudApiController;
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
public interface SimpleAsymmetricCrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, RD extends Identifiable<ID>, ID extends Serializable> extends AsymmetricCrudApiController<E, D, RD, ID> {

    @Override
    PageMapper<E, D> getMapper();

    @Override
    PageMapper<E, RD> getReferenceMapper();

    @GetMapping
    default ResponseEntity<Page<D>> getAll(final Pageable pageable) {
        return getService().findAll(pageable)
                .map(page -> getMapper().mapTo(page))
                .map(ResponseEntity::ok)
                .onFailure(e -> getLogger().error("Error while processing GET request", e))
                .getOrElseGet(e -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null));
    }
}
