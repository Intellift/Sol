package org.intellift.sol.domain;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis
 */
public interface Identifiable<ID extends Serializable> {

    ID getId();
}
