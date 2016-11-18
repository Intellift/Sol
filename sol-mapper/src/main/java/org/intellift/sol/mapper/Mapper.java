package org.intellift.sol.mapper;

import java.util.List;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface Mapper<E, D> {

    D mapTo(E object);

    E mapFrom(D object);

    List<D> mapTo(List<E> objects);

    List<E> mapFrom(List<D> objects);
}
