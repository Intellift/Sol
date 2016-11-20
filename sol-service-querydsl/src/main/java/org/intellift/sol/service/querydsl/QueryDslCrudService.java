package org.intellift.sol.service.querydsl;


import com.querydsl.core.types.Predicate;
import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.querydsl.repository.QueryDslRepository;
import org.intellift.sol.service.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface QueryDslCrudService<E extends Identifiable<ID>, ID extends Serializable> extends CrudService<E, ID> {

    @Override
    QueryDslRepository<E, ID> getEntityRepository();

    default Try<Page<E>> findAll(Predicate predicate, Pageable pageable) {
        return Try.of(() -> getEntityRepository().findAll(predicate, pageable));
    }
}
