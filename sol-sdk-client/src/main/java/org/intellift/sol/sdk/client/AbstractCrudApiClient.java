package org.intellift.sol.sdk.client;

import javaslang.Function2;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import javaslang.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.sdk.client.internal.PageResponseTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.client.RestOperations;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

import static org.intellift.sol.sdk.client.SdkUtils.buildUri;
import static org.intellift.sol.sdk.client.SdkUtils.flattenParameterValues;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public abstract class AbstractCrudApiClient<D extends Identifiable<ID>, ID extends Serializable> implements CrudApiClient<D, ID> {

    protected final RestOperations restOperations;

    public AbstractCrudApiClient(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    protected abstract Class<D> getDtoClass();

    protected abstract String getEndpoint();

    protected String getPageSizeParameterName() {
        return "size";
    }

    protected HttpHeaders getDefaultHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

        return httpHeaders;
    }

    @Override
    public final Page<D> getPage() {
        return getPage(Stream.empty());
    }

    @Override
    public Page<D> getPage(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        return buildUri(getEndpoint(), flattenParameterValues(parameters))
                .flatMap(uri -> Try.of(() -> restOperations.exchange(
                        uri,
                        HttpMethod.GET,
                        new HttpEntity<>(getDefaultHeaders()),
                        new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                        }
                )))
                .map(ResponseEntity::getBody)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);
    }

    @Override
    public final Page<D> getAll() {
        return getAll(Stream.empty());
    }

    @Override
    public Page<D> getAll(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        final String endpoint = getEndpoint();

        final HttpEntity<Void> httpEntity = new HttpEntity<>(getDefaultHeaders());

        final Stream<Tuple2<String, String>> parametersWithoutPageSize = flattenParameterValues(parameters)
                .filter(parameterNameValues -> !parameterNameValues._1.equals(getPageSizeParameterName()));

        final Try<Page<D>> firstPageTry = buildUri(endpoint, parametersWithoutPageSize)
                .flatMap(uri -> Try.of(() -> restOperations.exchange(
                        uri,
                        HttpMethod.GET,
                        httpEntity,
                        new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                        }
                )))
                .map(ResponseEntity::getBody);

        return firstPageTry
                .flatMap(firstPage -> {
                    final Long totalElements = firstPage.getTotalElements();
                    final Integer pageSize = firstPage.getSize();

                    return totalElements <= pageSize
                            ? Try.success(firstPage)
                            : parametersWithoutPageSize
                            .append(Tuple.of(getPageSizeParameterName(), String.valueOf(totalElements)))
                            .transform(Function2.of(SdkUtils::buildUri).apply(endpoint))
                            .flatMap(allElementsUri -> Try.of(() -> restOperations.exchange(
                                    allElementsUri,
                                    HttpMethod.GET,
                                    httpEntity,
                                    new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                                    }
                            )))
                            .map(ResponseEntity::getBody);
                })
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);
    }

    @Override
    public D getOne(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return Try
                .of(() -> restOperations.exchange(
                        String.join("/", getEndpoint(), String.valueOf(id)),
                        HttpMethod.GET,
                        new HttpEntity<>(getDefaultHeaders()),
                        getDtoClass()
                ))
                .map(ResponseEntity::getBody)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);
    }

    @Override
    public D create(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        return Try
                .of(() -> restOperations.exchange(
                        getEndpoint(),
                        HttpMethod.POST,
                        new HttpEntity<>(dto, getDefaultHeaders()),
                        getDtoClass()
                ))
                .map(ResponseEntity::getBody)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);
    }

    @Override
    public D replace(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        return Try
                .of(() -> restOperations.exchange(
                        String.join("/", getEndpoint(), String.valueOf(dto.getId())),
                        HttpMethod.PUT,
                        new HttpEntity<>(dto, getDefaultHeaders()),
                        getDtoClass()
                ))
                .map(ResponseEntity::getBody)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);
    }

    @Override
    public void delete(final ID id) {
        Objects.requireNonNull(id, "id is null");

        Try
                .of(() -> restOperations.exchange(
                        String.join("/", getEndpoint(), String.valueOf(id)),
                        HttpMethod.DELETE,
                        new HttpEntity<>(getDefaultHeaders()),
                        Void.class
                ))
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);
    }
}
