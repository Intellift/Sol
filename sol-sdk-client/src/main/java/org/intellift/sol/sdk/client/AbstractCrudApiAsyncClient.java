package org.intellift.sol.sdk.client;

import javaslang.Function2;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import javaslang.concurrent.Future;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.sdk.client.internal.PageResponseTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestOperations;

import java.io.Serializable;
import java.util.Objects;

import static javaslang.concurrent.Future.fromJavaFuture;
import static org.intellift.sol.sdk.client.SdkUtils.buildUri;
import static org.intellift.sol.sdk.client.SdkUtils.flattenParameterValues;

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

    protected HttpHeaders getDefaultHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

        return httpHeaders;
    }

    @Override
    public final Future<ResponseEntity<Page<D>>> getPage() {
        return getPage(Stream.empty());
    }

    @Override
    public Future<ResponseEntity<Page<D>>> getPage(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        final ListenableFuture<ResponseEntity<Page<D>>> future = asyncRestOperations.exchange(
                buildUri(getEndpoint(), flattenParameterValues(parameters)),
                HttpMethod.GET,
                new HttpEntity<>(getDefaultHeaders()),
                new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                }
        );

        return fromJavaFuture(future);
    }

    @Override
    public final Future<ResponseEntity<Page<D>>> getAll() {
        return getAll(Stream.empty());
    }

    @Override
    public Future<ResponseEntity<Page<D>>> getAll(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        final String endpoint = getEndpoint();

        final HttpEntity<Void> httpEntity = new HttpEntity<>(getDefaultHeaders());

        final Stream<Tuple2<String, String>> parametersWithoutPageSize = flattenParameterValues(parameters)
                .filter(parameterNameValues -> !parameterNameValues._1.equalsIgnoreCase(getPageSizeParameterName()));

        final ListenableFuture<ResponseEntity<Page<D>>> firstPageFuture = asyncRestOperations.exchange(
                buildUri(endpoint, parametersWithoutPageSize),
                HttpMethod.GET,
                httpEntity,
                new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                }
        );

        return fromJavaFuture(firstPageFuture)
                .flatMap(pageResponseEntity -> {
                    final Long totalElements = pageResponseEntity.getBody().getTotalElements();
                    final Integer pageSize = pageResponseEntity.getBody().getSize();

                    if (totalElements <= pageSize) {
                        return Future.successful(pageResponseEntity);
                    } else {
                        final String allElementsUrl = parametersWithoutPageSize
                                .append(Tuple.of(getPageSizeParameterName(), String.valueOf(totalElements)))
                                .transform(Function2.of(SdkUtils::buildUri).apply(endpoint));

                        final ListenableFuture<ResponseEntity<Page<D>>> allElementsFuture = asyncRestOperations.exchange(
                                allElementsUrl,
                                HttpMethod.GET,
                                httpEntity,
                                new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                                }
                        );

                        return fromJavaFuture(allElementsFuture);
                    }
                });
    }

    @Override
    public Future<ResponseEntity<D>> getOne(final ID id) {
        Objects.requireNonNull(id, "id is null");

        final ListenableFuture<ResponseEntity<D>> future = asyncRestOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(id)),
                HttpMethod.GET,
                new HttpEntity<>(getDefaultHeaders()),
                getDtoClass()
        );

        return fromJavaFuture(future);
    }

    @Override
    public Future<ResponseEntity<D>> create(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        final ListenableFuture<ResponseEntity<D>> future = asyncRestOperations.exchange(
                getEndpoint(),
                HttpMethod.POST,
                new HttpEntity<>(dto, getDefaultHeaders()),
                getDtoClass()
        );

        return fromJavaFuture(future);
    }

    @Override
    public Future<ResponseEntity<D>> replace(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        final ListenableFuture<ResponseEntity<D>> future = asyncRestOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(dto.getId())),
                HttpMethod.PUT,
                new HttpEntity<>(dto, getDefaultHeaders()),
                getDtoClass()
        );

        return fromJavaFuture(future);
    }

    @Override
    public Future<ResponseEntity<Void>> delete(final ID id) {
        Objects.requireNonNull(id, "id is null");

        final ListenableFuture<ResponseEntity<Void>> future = asyncRestOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(id)),
                HttpMethod.DELETE,
                new HttpEntity<>(getDefaultHeaders()),
                Void.class
        );

        return fromJavaFuture(future);
    }
}
