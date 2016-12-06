package org.intellift.sol.domain.querydsl.repository;


import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.repository.Repository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
@NoRepositoryBean
public interface QueryDslRepository<T extends Identifiable<ID>, ID extends Serializable> extends Repository<T, ID>, QueryDslPredicateExecutor<T> {
}
