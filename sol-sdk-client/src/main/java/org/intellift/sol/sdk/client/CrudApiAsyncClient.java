package org.intellift.sol.sdk.client;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.collection.Stream;
import javaslang.concurrent.Future;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.PageResponse;
import org.intellift.sol.sdk.client.config.CustomParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public abstract class CrudApiAsyncClient<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> {

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
    public final Future<ResponseEntity<PageResponse<D>>> getAll(final Tuple2<String, Iterable<String>>... parameters) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String url = getEndpoint();

        final UriComponentsBuilder uriComponentsBuilder = Stream.of(parameters)
                .map(t -> Tuple.of(t._1, List.ofAll(t._2)))
                .map(t -> t._2.size() > 1 ? Tuple.of(t._1 + "[]", t._2) : t)
                .flatMap(t -> t._2.map(value -> Tuple.of(t._1, value)))
                .foldLeft(UriComponentsBuilder.fromUriString(url), (builder, t) -> builder.queryParam(t._1, t._2));

        return convert(asyncRestOperations.exchange(
                uriComponentsBuilder.toUriString(),
                HttpMethod.GET,
                httpEntity,
                new CustomParameterizedTypeReference<PageResponse<D>>(getDtoClass()) {
                }
        ));
    }

    public Future<ResponseEntity<D>> getOne(final ID id) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String url = String.join("/", getEndpoint(), String.valueOf(id));

        return convert(asyncRestOperations.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                getDtoClass()
        ));
    }

    public Future<ResponseEntity<D>> create(final D dto) {
        final HttpEntity<D> httpEntity = new HttpEntity<>(dto, getHeaders());

        final String url = getEndpoint();

        return convert(asyncRestOperations.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                getDtoClass()
        ));
    }

    public Future<ResponseEntity<D>> update(final D dto) {
        final HttpEntity<D> httpEntity = new HttpEntity<>(dto, getHeaders());

        final String url = String.join("/", getEndpoint(), String.valueOf(dto.getId()));

        return convert(asyncRestOperations.exchange(
                url,
                HttpMethod.PUT,
                httpEntity,
                getDtoClass()
        ));
    }

    public Future<ResponseEntity<Void>> delete(final ID id) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String url = String.join("/", getEndpoint(), String.valueOf(id));

        return convert(asyncRestOperations.exchange(
                url,
                HttpMethod.DELETE,
                httpEntity,
                Void.class
        ));
    }
}
