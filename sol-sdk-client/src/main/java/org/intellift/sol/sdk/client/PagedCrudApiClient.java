package org.intellift.sol.sdk.client;

import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.concurrent.Future;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.PageResponse;
import org.intellift.sol.sdk.client.config.CustomParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public abstract class PagedCrudApiClient<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> extends CrudApiClient<E, D, ID> {

    public PagedCrudApiClient(AsyncRestOperations asyncRestOperations) {
        super(asyncRestOperations);
    }

    @SafeVarargs
    public final Future<ResponseEntity<PageResponse<D>>> getAll(final Tuple2<String, List<String>>... parameters) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String url = String.join("/", getBaseUrl(), getEndpoint());

        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

        List.of(parameters)
                .map(t -> t._2.size() > 1 ? new Tuple2<>(String.join("", t._1, "[]"), t._2) : t)
                .forEach(t -> t._2.forEach(value -> {
                    uriComponentsBuilder.queryParam(t._1, value);
                }));

        return convert(asyncRestOperations.exchange(
                uriComponentsBuilder.toUriString(),
                HttpMethod.GET,
                httpEntity,
                new CustomParameterizedTypeReference<PageResponse<D>>(getDtoClass()) {
                }
        ));
    }
}
