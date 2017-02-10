package org.intellift.sol.domain;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface Identifiable<ID extends Serializable> {

    ID getId();

    void setId(ID id);
}
