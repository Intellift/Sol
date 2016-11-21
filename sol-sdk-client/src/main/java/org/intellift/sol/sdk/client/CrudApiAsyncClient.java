package org.intellift.sol.sdk.client;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.collection.Stream;
import javaslang.concurrent.Future;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.sdk.client.internal.CustomParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public abstract class CrudApiAsyncClient<D extends Identifiable<ID>, ID extends Serializable> {

    protected final AsyncRestOperations asyncRestOperations;

    public CrudApiAsyncClient(AsyncRestOperations asyncRestOperations) {
        this.asyncRestOperations = asyncRestOperations;
    }

    public abstract Class<D> getDtoClass();

    public abstract String getEndpoint();

    public HttpHeaders getHeaders() {
        final HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        return headers;
    }

    protected <T> Future<T> convert(final ListenableFuture<T> listenableFuture) {
        return Future.fromJavaFuture(listenableFuture);
    }

    @SafeVarargs
    public final Future<ResponseEntity<Page<D>>> getAll(final Tuple2<String, Iterable<String>>... parameters) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String endpoint = getEndpoint();

        final String uri = Stream.of(parameters)
                .map(t -> Tuple.of(t._1, List.ofAll(t._2)))
                .map(t -> t._2.size() > 1 ? Tuple.of(t._1 + "[]", t._2) : t)
                .flatMap(t -> t._2.map(value -> Tuple.of(t._1, value)))
                .foldLeft(UriComponentsBuilder.fromUriString(endpoint), (builder, t) -> builder.queryParam(t._1, t._2))
                .toUriString();

        return convert(asyncRestOperations.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                new CustomParameterizedTypeReference<Page<D>>(getDtoClass()) {
                }
        ));
    }

    public Future<ResponseEntity<D>> getOne(final ID id) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String uri = String.join("/", getEndpoint(), String.valueOf(id));

        return convert(asyncRestOperations.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                getDtoClass()
        ));
    }

    public Future<ResponseEntity<D>> create(final D dto) {
        final HttpEntity<D> httpEntity = new HttpEntity<>(dto, getHeaders());

        final String uri = getEndpoint();

        return convert(asyncRestOperations.exchange(
                uri,
                HttpMethod.POST,
                httpEntity,
                getDtoClass()
        ));
    }

    public Future<ResponseEntity<D>> update(final D dto) {
        final HttpEntity<D> httpEntity = new HttpEntity<>(dto, getHeaders());

        final String uri = String.join("/", getEndpoint(), String.valueOf(dto.getId()));

        return convert(asyncRestOperations.exchange(
                uri,
                HttpMethod.PUT,
                httpEntity,
                getDtoClass()
        ));
    }

    public Future<ResponseEntity<Void>> delete(final ID id) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String uri = String.join("/", getEndpoint(), String.valueOf(id));

        return convert(asyncRestOperations.exchange(
                uri,
                HttpMethod.DELETE,
                httpEntity,
                Void.class
        ));
    }
}
