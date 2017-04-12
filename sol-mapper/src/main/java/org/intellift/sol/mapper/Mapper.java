package org.intellift.sol.mapper;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface Mapper<E, D> {

    D mapTo(E object);

    E mapFrom(D object);
}
