package org.intellift.sol.controller.api;

import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.mapper.Mapper;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface CrudApiController<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> extends AsymmetricCrudApiController<E, D, D, ID> {

    @Override
    default Mapper<E, D> getReferenceMapper() {
        return getMapper();
    }
}
