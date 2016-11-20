package org.intellift.sol.domain.repository;

import org.intellift.sol.domain.Identifiable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface Repository<E extends Identifiable<ID>, ID extends Serializable> extends PagingAndSortingRepository<E, ID> {
}
