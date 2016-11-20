package org.intellift.sol.sdk.client;

import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.concurrent.Future;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.PageResponse;
import org.intellift.sol.sdk.client.config.CustomParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public abstract class CrudApiClient<E extends Identifiable<ID>, D extends Identifiable<ID>, ID extends Serializable> {

    protected final AsyncRestOperations asyncRestOperations;

    public CrudApiClient(AsyncRestOperations asyncRestOperations) {
        this.asyncRestOperations = asyncRestOperations;
    }

    public abstract Class<D> getDtoClass();

    public abstract String getBaseUrl();

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

    public Future<ResponseEntity<D>> getOne(final ID id) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String url = String.join("/", getBaseUrl(), getEndpoint(), (String) id);

        return convert(asyncRestOperations.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                getDtoClass()
        ));
    }

    public Future<ResponseEntity<D>> create(final D dto) {
        final HttpEntity<D> httpEntity = new HttpEntity<>(dto, getHeaders());

        final String url = String.join("/", getBaseUrl(), getEndpoint());

        return convert(asyncRestOperations.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                getDtoClass()
        ));
    }

    public Future<ResponseEntity<D>> update(final D dto) {
        final HttpEntity<D> httpEntity = new HttpEntity<>(dto, getHeaders());

        final String url = String.join("/", getBaseUrl(), getEndpoint(), (String) dto.getId());

        return convert(asyncRestOperations.exchange(
                url,
                HttpMethod.PUT,
                httpEntity,
                getDtoClass()
        ));
    }

    public Future<ResponseEntity<Void>> delete(final ID id) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String url = String.join("/", getBaseUrl(), getEndpoint(), (String) id);

        return convert(asyncRestOperations.exchange(
                url,
                HttpMethod.DELETE,
                httpEntity,
                Void.class
        ));
    }
}
