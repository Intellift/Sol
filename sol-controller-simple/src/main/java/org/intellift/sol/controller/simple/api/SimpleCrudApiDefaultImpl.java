package org.intellift.sol.controller.simple.api;

import javaslang.Function1;
import javaslang.control.Try;
import org.intellift.sol.controller.api.CrudApiDefaultImpl;
import org.intellift.sol.domain.Identifiable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author Achilleas Naoumidis
 */
public abstract class SimpleCrudApiDefaultImpl extends CrudApiDefaultImpl {

    public static <E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable>
    Function1<Pageable, Try<ResponseEntity<Page<D>>>> getAll(final Function<Pageable, Try<Page<E>>> findAll,
                                                             final Function<Page<E>, Page<D>> toDTO) {
        return (final Pageable pageable) -> findAll.apply(pageable)
                .map(toDTO)
                .map(ResponseEntity::ok);
    }
}
