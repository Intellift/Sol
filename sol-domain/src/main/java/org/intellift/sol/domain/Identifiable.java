package org.intellift.sol.domain;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface Identifiable<ID extends Serializable> {

    static <T extends Identifiable<ID>, ID extends Serializable> T instantiateWithId(final Class<T> clazz, final ID id) {
        try {
            final T identifiable = clazz.newInstance();

            identifiable.setId(id);

            return identifiable;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    ID getId();

    void setId(ID id);
}
