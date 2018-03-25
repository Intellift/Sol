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
    public final Future<Page<D>> getPage() {
        return getPage(Stream.empty());
    }

    @Override
    public Future<Page<D>> getPage(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        return Future.fromTry(buildUri(getEndpoint(), flattenParameterValues(parameters)))
                .map(uri -> asyncRestOperations.exchange(
                        uri,
                        HttpMethod.GET,
                        new HttpEntity<>(getDefaultHeaders()),
                        new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                        }
                ))
                .flatMap(Future::fromJavaFuture)
                .map(HttpEntity::getBody);
    }

    @Override
    public final Future<Page<D>> getAll() {
        return getAll(Stream.empty());
    }

    @Override
    public Future<Page<D>> getAll(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        final String endpoint = getEndpoint();

        final HttpEntity<Void> httpEntity = new HttpEntity<>(getDefaultHeaders());

        final Stream<Tuple2<String, String>> parametersWithoutPageSize = flattenParameterValues(parameters)
                .filter(parameterNameValues -> !parameterNameValues._1.equalsIgnoreCase(getPageSizeParameterName()));

        final Future<Page<D>> firstPageFuture = Future.fromTry(buildUri(endpoint, parametersWithoutPageSize))
                .map(uri -> asyncRestOperations.exchange(
                        uri,
                        HttpMethod.GET,
                        httpEntity,
                        new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                        }
                ))
                .flatMap(Future::fromJavaFuture)
                .map(HttpEntity::getBody);

        return firstPageFuture
                .flatMap(page -> {
                    final Long totalElements = page.getTotalElements();
                    final Integer pageSize = page.getSize();

                    return totalElements <= pageSize
                            ? Future.successful(page)
                            : parametersWithoutPageSize
                            .append(Tuple.of(getPageSizeParameterName(), String.valueOf(totalElements)))
                            .transform(Function2.of(SdkUtils::buildUri).apply(endpoint))
                            .transform(Future::fromTry)
                            .map(allElementsUri -> asyncRestOperations.exchange(
                                    allElementsUri,
                                    HttpMethod.GET,
                                    httpEntity,
                                    new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                                    }
                            ))
                            .flatMap(Future::fromJavaFuture)
                            .map(HttpEntity::getBody);
                });
    }

    @Override
    public Future<D> getOne(final ID id) {
        Objects.requireNonNull(id, "id is null");

        final ListenableFuture<ResponseEntity<D>> future = asyncRestOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(id)),
                HttpMethod.GET,
                new HttpEntity<>(getDefaultHeaders()),
                getDtoClass()
        );

        return fromJavaFuture(future)
                .map(HttpEntity::getBody);
    }

    @Override
    public Future<D> create(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        final ListenableFuture<ResponseEntity<D>> future = asyncRestOperations.exchange(
                getEndpoint(),
                HttpMethod.POST,
                new HttpEntity<>(dto, getDefaultHeaders()),
                getDtoClass()
        );

        return fromJavaFuture(future)
                .map(HttpEntity::getBody);
    }

    @Override
    public Future<D> replace(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        final ListenableFuture<ResponseEntity<D>> future = asyncRestOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(dto.getId())),
                HttpMethod.PUT,
                new HttpEntity<>(dto, getDefaultHeaders()),
                getDtoClass()
        );

        return fromJavaFuture(future)
                .map(HttpEntity::getBody);
    }

    @Override
    public Future<Void> delete(final ID id) {
        Objects.requireNonNull(id, "id is null");

        final ListenableFuture<ResponseEntity<Void>> future = asyncRestOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(id)),
                HttpMethod.DELETE,
                new HttpEntity<>(getDefaultHeaders()),
                Void.class
        );

        return fromJavaFuture(future)
                .map(HttpEntity::getBody);
    }
}
