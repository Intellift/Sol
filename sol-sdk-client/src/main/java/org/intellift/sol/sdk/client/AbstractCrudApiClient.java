package org.intellift.sol.sdk.client;

import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.sdk.client.internal.PageResponseTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestOperations;

import java.io.Serializable;
import java.util.Objects;

import static io.vavr.API.Stream;
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

        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

        return httpHeaders;
    }

    @Override
    public final Page<D> getPage() {
        return getPage(Stream());
    }

    @Override
    public Page<D> getPage(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        return restOperations
                .exchange(
                        buildUri(getEndpoint(), flattenParameterValues(parameters)),
                        HttpMethod.GET,
                        new HttpEntity<>(getDefaultHeaders()),
                        new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                        }
                )
                .getBody();
    }

    @Override
    public final Page<D> getAll() {
        return getAll(Stream());
    }

    @Override
    public Page<D> getAll(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        final String endpoint = getEndpoint();

        final HttpEntity<Void> httpEntity = new HttpEntity<>(getDefaultHeaders());

        final Stream<Tuple2<String, String>> parametersWithoutPageSize = flattenParameterValues(parameters)
                .filter(parameterNameValues -> !parameterNameValues._1.equals(getPageSizeParameterName()));

        final Page<D> firstPage = restOperations
                .exchange(
                        buildUri(endpoint, parametersWithoutPageSize),
                        HttpMethod.GET,
                        httpEntity,
                        new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                        }
                )
                .getBody();

        final Long totalElements = firstPage.getTotalElements();
        final Integer pageSize = firstPage.getSize();

        if (totalElements <= pageSize) {
            return firstPage;
        } else {
            final String allElementsUrl = parametersWithoutPageSize
                    .append(Tuple.of(getPageSizeParameterName(), String.valueOf(totalElements)))
                    .transform(Function2.of(SdkUtils::buildUri).apply(endpoint));

            return restOperations
                    .exchange(
                            allElementsUrl,
                            HttpMethod.GET,
                            httpEntity,
                            new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                            }
                    )
                    .getBody();
        }
    }

    @Override
    public D getOne(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return restOperations
                .exchange(
                        String.join("/", getEndpoint(), String.valueOf(id)),
                        HttpMethod.GET,
                        new HttpEntity<>(getDefaultHeaders()),
                        getDtoClass()
                )
                .getBody();
    }

    @Override
    public D create(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        return restOperations
                .exchange(
                        getEndpoint(),
                        HttpMethod.POST,
                        new HttpEntity<>(dto, getDefaultHeaders()),
                        getDtoClass()
                )
                .getBody();
    }

    @Override
    public D replace(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        return restOperations
                .exchange(
                        String.join("/", getEndpoint(), String.valueOf(dto.getId())),
                        HttpMethod.PUT,
                        new HttpEntity<>(dto, getDefaultHeaders()),
                        getDtoClass()
                )
                .getBody();
    }

    @Override
    public void delete(final ID id) {
        Objects.requireNonNull(id, "id is null");

        restOperations
                .exchange(
                        String.join("/", getEndpoint(), String.valueOf(id)),
                        HttpMethod.DELETE,
                        new HttpEntity<>(getDefaultHeaders()),
                        Void.class
                )
                .getBody();
    }
}
