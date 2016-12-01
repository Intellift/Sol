package org.intellift.sol.controller.querydsl.api;

import com.querydsl.core.types.Predicate;
import org.intellift.sol.controller.api.CrudApiController;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.mapper.PageMapper;
import org.intellift.sol.service.querydsl.QueryDslCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface QueryDslCrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> extends CrudApiController<E, D, ID> {

    @Override
    PageMapper<E, D> getEntityMapper();

    @Override
    QueryDslCrudService<E, ID> getEntityService();

    @GetMapping
    ResponseEntity<Page<D>> getAll(Predicate predicate, Pageable pageable);

    default ResponseEntity<Page<D>> getAllDefaultImplementation(Predicate predicate, Pageable pageable) {
        return getEntityService().findAll(predicate, pageable)
                .map(page -> getEntityMapper().mapTo(page))
                .map(ResponseEntity::ok)
                .onFailure(e -> getLogger().error("", e))
                .getOrElseGet(e -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null));
    }
}
