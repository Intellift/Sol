package org.intellift.sol.sdk.client;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.collection.Stream;
import javaslang.concurrent.Future;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.sdk.client.internal.PageResponseTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public abstract class AbstractCrudApiAsyncClient<D extends Identifiable<ID>, ID extends Serializable> implements CrudApiAsyncClient<D, ID> {

    protected final AsyncRestOperations asyncRestOperations;

    public AbstractCrudApiAsyncClient(AsyncRestOperations asyncRestOperations) {
        this.asyncRestOperations = asyncRestOperations;
    }

    protected abstract Class<D> getDtoClass();

    protected abstract String getEndpoint();

    protected String getPageSizeParameterName() {
        return "size";
    }

    protected HttpHeaders getHeaders() {
        final HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        return headers;
    }

    protected <T> Future<T> convert(final ListenableFuture<T> listenableFuture) {
        return Future.fromJavaFuture(listenableFuture);
    }

    @Override
    public final Future<ResponseEntity<Page<D>>> getPage(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String endpoint = getEndpoint();

        final String uri = Stream.ofAll(parameters)
                .map(t -> Tuple.of(t._1, Stream.ofAll(t._2)))
                .map(t -> t._2.size() > 1 ? Tuple.of(t._1 + "[]", t._2) : t)
                .flatMap(t -> t._2.map(value -> Tuple.of(t._1, value)))
                .foldLeft(UriComponentsBuilder.fromUriString(endpoint), (builder, t) -> builder.queryParam(t._1, t._2))
                .toUriString();

        return convert(asyncRestOperations.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                }
        ));
    }

    @Override
    public final Future<ResponseEntity<Page<D>>> getAll(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        final HttpEntity<Void> httpEntity = new HttpEntity<>(getHeaders());

        final String endpoint = getEndpoint();

        final Seq<Tuple2<String, String>> processedQuery = Stream.ofAll(parameters)
                .filter(tuple -> !tuple._1.equalsIgnoreCase(getPageSizeParameterName()))
                .map(t -> Tuple.of(t._1, Stream.ofAll(t._2)))
                .map(t -> t._2.size() > 1 ? Tuple.of(t._1 + "[]", t._2) : t)
                .flatMap(t -> t._2.map(value -> Tuple.of(t._1, value)));

        final String metadataQueryUri = processedQuery
                .append(Tuple.of(getPageSizeParameterName(), "0"))
                .foldLeft(UriComponentsBuilder.fromUriString(endpoint), (builder, t) -> builder.queryParam(t._1, t._2))
                .toUriString();

        return convert(
                asyncRestOperations.exchange(
                        metadataQueryUri,
                        HttpMethod.GET,
                        httpEntity,
                        new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                        }
                ))
                .flatMap(pageResponseEntity -> {
                    final String allElementsQueryUri = processedQuery
                            .append(Tuple.of(getPageSizeParameterName(), String.valueOf(pageResponseEntity.getBody().getTotalElements())))
                            .foldLeft(UriComponentsBuilder.fromUriString(endpoint), (builder, t) -> builder.queryParam(t._1, t._2))
                            .toUriString();

                    return convert(asyncRestOperations.exchange(
                            allElementsQueryUri,
                            HttpMethod.GET,
                            httpEntity,
                            new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                            }
                    ));
                });
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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