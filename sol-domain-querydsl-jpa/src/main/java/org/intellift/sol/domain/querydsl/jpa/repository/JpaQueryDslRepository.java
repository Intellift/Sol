package org.intellift.sol.domain.querydsl.jpa.repository;

import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.jpa.repository.JpaRepository;
import org.intellift.sol.domain.querydsl.executor.CustomQueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
@NoRepositoryBean
public interface JpaQueryDslRepository<T extends Identifiable<ID>, ID extends Serializable> extends JpaRepository<T, ID>, org.intellift.sol.domain.repository.Repository<T, ID>, CustomQueryDslPredicateExecutor<T> {
}
