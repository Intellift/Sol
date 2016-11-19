package org.intellift.sol.domain.querydsl.jpa.repository;

import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.jpa.repository.JpaRepository;
import org.intellift.sol.domain.querydsl.repository.QueryDslRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
@NoRepositoryBean
public interface QueryDslJpaRepository<T extends Identifiable<ID>, ID extends Serializable> extends QueryDslRepository<T, ID>, JpaRepository<T, ID> {
}
