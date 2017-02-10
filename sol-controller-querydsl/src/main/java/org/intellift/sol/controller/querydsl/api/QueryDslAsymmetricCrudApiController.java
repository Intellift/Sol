package org.intellift.sol.controller.querydsl.api;

import com.querydsl.core.types.Predicate;
import org.intellift.sol.controller.api.AsymmetricCrudApiController;
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
public interface QueryDslAsymmetricCrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, RD extends Identifiable<ID>, ID extends Serializable> extends AsymmetricCrudApiController<E, D, RD, ID> {

    @Override
    PageMapper<E, D> getMapper();

    @Override
    PageMapper<E, RD> getReferenceMapper();

    @Override
    QueryDslCrudService<E, ID> getService();

    @GetMapping
    ResponseEntity<Page<D>> getAll(Predicate predicate, Pageable pageable);

    default ResponseEntity<Page<D>> getAllDefaultImplementation(final Predicate predicate, final Pageable pageable) {
        return getService().findAll(predicate, pageable)
                .map(page -> getMapper().mapTo(page))
                .map(ResponseEntity::ok)
                .onFailure(throwable -> getLogger().error("Error occurred while processing GET request", throwable))
                .getOrElse(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null));
    }
}
