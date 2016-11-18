package org.intellift.sol.controller.simple.api;

import org.intellift.sol.controller.api.CrudApiController;
import org.intellift.sol.domain.Identifiable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface SimpleCrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> extends CrudApiController<E, D, ID> {

    @GetMapping
    default ResponseEntity<List<D>> getAll() {
        final List<E> entities = getEntityService().findAll()
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new)
                .toJavaList();

        final List<D> dto = getEntityMapper().mapTo(entities);

        return ResponseEntity.ok(dto);
    }
}
