package org.intellift.sol.domain.querydsl.repository;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.repository.Repository;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
@NoRepositoryBean
public interface QueryDslRepository<T extends Identifiable<ID>, ID extends Serializable> extends Repository<T, ID>, QueryDslPredicateExecutor<T> {

    @Override
    List<T> findAll(Predicate predicate);

    @Override
    List<T> findAll(Predicate predicate, Sort sort);

    @Override
    List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);

    @Override
    List<T> findAll(OrderSpecifier<?>... orders);
}
