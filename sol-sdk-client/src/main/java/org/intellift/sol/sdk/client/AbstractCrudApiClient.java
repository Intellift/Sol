package org.intellift.sol.sdk.client;

import javaslang.Function2;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.sdk.client.internal.PageResponseTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.io.Serializable;
import java.util.Objects;

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
        return new HttpHeaders();
    }

    @Override
    public final ResponseEntity<Page<D>> getPage() {
        return getPage(Stream.empty());
    }

    @Override
    public ResponseEntity<Page<D>> getPage(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        return restOperations.exchange(
                buildUri(getEndpoint(), flattenParameterValues(parameters)),
                HttpMethod.GET,
                new HttpEntity<>(getDefaultHeaders()),
                new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                }
        );
    }

    @Override
    public final ResponseEntity<Page<D>> getAll() {
        return getAll(Stream.empty());
    }

    @Override
    public ResponseEntity<Page<D>> getAll(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        final String endpoint = getEndpoint();

        final HttpEntity<Void> httpEntity = new HttpEntity<>(getDefaultHeaders());

        final Stream<Tuple2<String, String>> parametersWithoutPageSize = flattenParameterValues(parameters)
                .filter(parameterNameValues -> !parameterNameValues._1.equals(getPageSizeParameterName()));

        final ResponseEntity<Page<D>> firstPage = restOperations.exchange(
                buildUri(endpoint, parametersWithoutPageSize),
                HttpMethod.GET,
                httpEntity,
                new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                }
        );

        final Long totalElements = firstPage.getBody().getTotalElements();
        final Integer pageSize = firstPage.getBody().getSize();

        if (totalElements <= pageSize) {
            return firstPage;
        } else {
            final String allElementsUrl = parametersWithoutPageSize
                    .append(Tuple.of(getPageSizeParameterName(), String.valueOf(totalElements)))
                    .transform(Function2.of(SdkUtils::buildUri).apply(endpoint));

            return restOperations.exchange(
                    allElementsUrl,
                    HttpMethod.GET,
                    httpEntity,
                    new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                    }
            );
        }
    }

    @Override
    public ResponseEntity<D> getOne(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return restOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(id)),
                HttpMethod.GET,
                new HttpEntity<>(getDefaultHeaders()),
                getDtoClass()
        );
    }

    @Override
    public ResponseEntity<D> create(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        return restOperations.exchange(
                getEndpoint(),
                HttpMethod.POST,
                new HttpEntity<>(dto, getDefaultHeaders()),
                getDtoClass()
        );
    }

    @Override
    public ResponseEntity<D> replace(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        return restOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(dto.getId())),
                HttpMethod.PUT,
                new HttpEntity<>(dto, getDefaultHeaders()),
                getDtoClass()
        );
    }

    @Override
    public ResponseEntity<Void> delete(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return restOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(id)),
                HttpMethod.DELETE,
                new HttpEntity<>(getDefaultHeaders()),
                Void.class
        );
    }
}
