package org.intellift.sol.mapper;

public interface Mapper<E, D> {

    D mapTo(E object);

    E mapFrom(D object);
}
