package org.intellift.sol.sdk.client;

import javaslang.Tuple2;
import javaslang.control.Option;
import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.springframework.data.domain.Page;

import java.io.Serializable;

/**
 * @author Chrisostomos Bakouras.
 */
public interface CrudApiClient<D extends Identifiable<ID>, ID extends Serializable> {

    Try<Page<D>> getAll();

    Try<Page<D>> getAll(Iterable<Tuple2<String, ? extends Iterable<String>>> parameters);

    Try<Page<D>> getPage();

    Try<Page<D>> getPage(Iterable<Tuple2<String, ? extends Iterable<String>>> parameters);

    Try<Option<D>> getOne(ID id);

    Try<D> create(D dto);

    Try<D> replace(D dto);

    Try<Void> delete(ID id);
}
