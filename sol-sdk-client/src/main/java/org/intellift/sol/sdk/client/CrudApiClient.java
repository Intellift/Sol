package org.intellift.sol.sdk.client;

import io.vavr.Tuple2;
import org.intellift.sol.domain.Identifiable;
import org.springframework.data.domain.Page;

import java.io.Serializable;

/**
 * @author Chrisostomos Bakouras.
 */
public interface CrudApiClient<D extends Identifiable<ID>, ID extends Serializable> {

    Page<D> getAll();

    Page<D> getAll(Iterable<Tuple2<String, Iterable<String>>> parameters);

    Page<D> getPage();

    Page<D> getPage(Iterable<Tuple2<String, Iterable<String>>> parameters);

    D getOne(ID id);

    D create(D dto);

    D replace(D dto);

    void delete(ID id);
}
