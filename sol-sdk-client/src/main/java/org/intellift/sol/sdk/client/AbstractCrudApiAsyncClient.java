package org.intellift.sol.sdk.client;

import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.sdk.client.internal.PageResponseTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.client.HttpClientErrorException;

import java.io.Serializable;
import java.util.Objects;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.concurrent.Future.fromJavaFuture;
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
        return getPage(Stream());
    }

    @Override
    public Future<Page<D>> getPage(final Iterable<Tuple2<String, ? extends Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        final ListenableFuture<ResponseEntity<Page<D>>> future = asyncRestOperations.exchange(
                buildUri(getEndpoint(), flattenParameterValues(parameters)),
                HttpMethod.GET,
                new HttpEntity<>(getDefaultHeaders()),
                new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                }
        );

        return fromJavaFuture(future)
                .map(ResponseEntity::getBody);
    }

    @Override
    public final Future<Page<D>> getAll() {
        return getAll(Stream());
    }

    @Override
    public Future<Page<D>> getAll(final Iterable<Tuple2<String, ? extends Iterable<String>>> parameters) {
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
                .map(ResponseEntity::getBody)
                .flatMap(page -> {
                    final Long totalElements = page.getTotalElements();
                    final Integer pageSize = page.getSize();

                    if (totalElements <= pageSize) {
                        return Future.successful(page);
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

                        return fromJavaFuture(allElementsFuture)
                                .map(ResponseEntity::getBody);
                    }
                });
    }

    @Override
    public Future<Option<D>> getOne(final ID id) {
        Objects.requireNonNull(id, "id is null");

        final ListenableFuture<ResponseEntity<D>> future = asyncRestOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(id)),
                HttpMethod.GET,
                new HttpEntity<>(getDefaultHeaders()),
                getDtoClass()
        );

        return fromJavaFuture(future)
                .map(ResponseEntity::getBody)
                .map(Option::of)
                .recoverWith(throwable -> Match(throwable).of(

                        Case($(instanceOf(HttpClientErrorException.class)), e -> {
                            if (e.getRawStatusCode() == HttpStatus.NOT_FOUND.value()) {
                                return Future.successful(Option.<D>none());
                            } else {
                                return Future.<Option<D>>failed(e);
                            }
                        }),

                        Case($(), Future::failed)));
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
                .map(ResponseEntity::getBody);
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
                .map(ResponseEntity::getBody);
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
                .map(ResponseEntity::getBody);
    }
}
