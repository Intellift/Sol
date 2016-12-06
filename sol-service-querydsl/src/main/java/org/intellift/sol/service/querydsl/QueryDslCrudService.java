package org.intellift.sol.service.querydsl;


import com.querydsl.core.types.Predicate;
import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.querydsl.repository.QueryDslRepository;
import org.intellift.sol.service.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface QueryDslCrudService<E extends Identifiable<ID>, ID extends Serializable> extends CrudService<E, ID> {

    @Override
    QueryDslRepository<E, ID> getEntityRepository();

    default Try<Page<E>> findAll(final Predicate predicate, final Pageable pageable) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(pageable, "pageable is null");

        return Try.of(() -> getEntityRepository().findAll(predicate, pageable));
    }
}
