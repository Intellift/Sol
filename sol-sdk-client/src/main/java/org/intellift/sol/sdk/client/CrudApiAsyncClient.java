package org.intellift.sol.sdk.client;

import javaslang.Tuple2;
import javaslang.concurrent.Future;
import org.intellift.sol.domain.Identifiable;
import org.springframework.data.domain.Page;

import java.io.Serializable;

/**
 * @author Chrisostomos Bakouras.
 */
public interface CrudApiAsyncClient<D extends Identifiable<ID>, ID extends Serializable> {

    Future<Page<D>> getAll();

    Future<Page<D>> getAll(Iterable<Tuple2<String, Iterable<String>>> parameters);

    Future<Page<D>> getPage();

    Future<Page<D>> getPage(Iterable<Tuple2<String, Iterable<String>>> parameters);

    Future<D> getOne(ID id);

    Future<D> create(D dto);

    Future<D> replace(D dto);

    Future<Void> delete(ID id);
}
