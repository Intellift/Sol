package org.intellift.sol.domain.jpa.repository;


import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.repository.Repository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
@NoRepositoryBean
public interface JpaRepository<T extends Identifiable<ID>, ID extends Serializable> extends Repository<T, ID>, org.springframework.data.jpa.repository.JpaRepository<T, ID> {
}
