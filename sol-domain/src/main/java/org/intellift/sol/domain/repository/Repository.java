package org.intellift.sol.domain.repository;

import org.intellift.sol.domain.Identifiable;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface Repository<E extends Identifiable<ID>, ID extends Serializable> extends CrudRepository<E, ID> {
}
