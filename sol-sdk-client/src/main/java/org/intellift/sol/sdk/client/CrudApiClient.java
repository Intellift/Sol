package org.intellift.sol.sdk.client;

import javaslang.Tuple2;
import org.intellift.sol.domain.Identifiable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * @author Chrisostomos Bakouras.
 */
public interface CrudApiClient<D extends Identifiable<ID>, ID extends Serializable> {

    ResponseEntity<Page<D>> getAll();

    ResponseEntity<Page<D>> getAll(Iterable<Tuple2<String, Iterable<String>>> parameters);

    ResponseEntity<Page<D>> getPage();

    ResponseEntity<Page<D>> getPage(Iterable<Tuple2<String, Iterable<String>>> parameters);

    ResponseEntity<D> getOne(ID id);

    ResponseEntity<D> create(D dto);

    ResponseEntity<D> replace(D dto);

    ResponseEntity<Void> delete(ID id);
}
