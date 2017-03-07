package org.intellift.sol.service.querydsl;


import com.querydsl.core.types.Predicate;
import javaslang.control.Option;
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
    QueryDslRepository<E, ID> getRepository();

    default Try<Page<E>> findAll(final Predicate predicate, final Pageable pageable) {
        return Try.of(() -> getRepository().findAll(predicate, pageable));
    }

    default Try<Option<E>> findOne(final Predicate predicate) {
        return Try.of(() -> Option.of(getRepository().findOne(predicate)));
    }
}
