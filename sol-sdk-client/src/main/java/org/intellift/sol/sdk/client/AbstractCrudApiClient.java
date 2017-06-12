package org.intellift.sol.sdk.client;

import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.sdk.client.internal.PageResponseTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;

import java.io.Serializable;
import java.util.Objects;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
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
    public final Try<Page<D>> getPage() {
        return getPage(Stream());
    }

    @Override
    public Try<Page<D>> getPage(final Iterable<Tuple2<String, ? extends Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        return Try
                .of(() -> restOperations.exchange(
                        buildUri(getEndpoint(), flattenParameterValues(parameters)),
                        HttpMethod.GET,
                        new HttpEntity<>(getDefaultHeaders()),
                        new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                        }))
                .map(ResponseEntity::getBody);
    }

    @Override
    public final Try<Page<D>> getAll() {
        return getAll(Stream());
    }

    @Override
    public Try<Page<D>> getAll(final Iterable<Tuple2<String, ? extends Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        final String endpoint = getEndpoint();

        final HttpEntity<Void> httpEntity = new HttpEntity<>(getDefaultHeaders());

        final Stream<Tuple2<String, String>> parametersWithoutPageSize = flattenParameterValues(parameters)
                .filter(parameterNameValues -> !parameterNameValues._1.equals(getPageSizeParameterName()));

        return Try
                .of(() -> restOperations.exchange(
                        buildUri(endpoint, parametersWithoutPageSize),
                        HttpMethod.GET,
                        httpEntity,
                        new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                        }))
                .map(ResponseEntity::getBody)
                .flatMap(page -> {
                    final Long totalElements = page.getTotalElements();
                    final Integer pageSize = page.getSize();

                    if (totalElements <= pageSize) {
                        return Try.success(page);
                    } else {
                        final String allElementsUrl = parametersWithoutPageSize
                                .append(Tuple.of(getPageSizeParameterName(), String.valueOf(totalElements)))
                                .transform(Function2.of(SdkUtils::buildUri).apply(endpoint));

                        return Try
                                .of(() -> restOperations.exchange(
                                        allElementsUrl,
                                        HttpMethod.GET,
                                        httpEntity,
                                        new PageResponseTypeReference<Page<D>>(getDtoClass()) {
                                        }))
                                .map(ResponseEntity::getBody);
                    }
                });
    }

    @Override
    public Try<Option<D>> getOne(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return Try
                .of(() -> restOperations.exchange(
                        String.join("/", getEndpoint(), String.valueOf(id)),
                        HttpMethod.GET,
                        new HttpEntity<>(getDefaultHeaders()),
                        getDtoClass()))
                .map(ResponseEntity::getBody)
                .map(Option::of)
                .recoverWith(throwable -> Match(throwable).of(

                        Case($(instanceOf(HttpClientErrorException.class)), e -> {
                            if (e.getRawStatusCode() == HttpStatus.NOT_FOUND.value()) {
                                return Try.success(Option.<D>none());
                            } else {
                                return Try.<Option<D>>failure(e);
                            }
                        }),

                        Case($(), Try::failure)));
    }

    @Override
    public Try<D> create(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        return Try
                .of(() -> restOperations.exchange(
                        getEndpoint(),
                        HttpMethod.POST,
                        new HttpEntity<>(dto, getDefaultHeaders()),
                        getDtoClass()))
                .map(ResponseEntity::getBody);
    }

    @Override
    public Try<D> replace(final D dto) {
        Objects.requireNonNull(dto, "dto is null");

        return Try
                .of(() -> restOperations.exchange(
                        String.join("/", getEndpoint(), String.valueOf(dto.getId())),
                        HttpMethod.PUT,
                        new HttpEntity<>(dto, getDefaultHeaders()),
                        getDtoClass()))
                .map(ResponseEntity::getBody);
    }

    @Override
    public Try<Void> delete(final ID id) {
        Objects.requireNonNull(id, "id is null");

        return Try.run(() -> restOperations.exchange(
                String.join("/", getEndpoint(), String.valueOf(id)),
                HttpMethod.DELETE,
                new HttpEntity<>(getDefaultHeaders()),
                Void.class
        ));
    }
}
