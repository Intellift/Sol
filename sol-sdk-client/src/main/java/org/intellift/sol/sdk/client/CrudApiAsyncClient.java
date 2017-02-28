package org.intellift.sol.sdk.client;

import javaslang.Tuple2;
import javaslang.concurrent.Future;
import org.intellift.sol.domain.Identifiable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * @author Chrisostomos Bakouras.
 */
public interface CrudApiAsyncClient<D extends Identifiable<ID>, ID extends Serializable> {

    Future<ResponseEntity<Page<D>>> getAll();

    Future<ResponseEntity<Page<D>>> getAll(Iterable<Tuple2<String, Iterable<String>>> parameters);

    Future<ResponseEntity<Page<D>>> getPage();

    Future<ResponseEntity<Page<D>>> getPage(Iterable<Tuple2<String, Iterable<String>>> parameters);

    Future<ResponseEntity<D>> getOne(ID id);

    Future<ResponseEntity<D>> create(D dto);

    Future<ResponseEntity<D>> replace(D dto);

    Future<ResponseEntity<Void>> delete(ID id);
}
