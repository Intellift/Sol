package org.intellift.sol.sdk.client;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Foldable;
import javaslang.collection.Stream;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

/**
 * @author Achilleas Naoumidis
 */
public abstract class SdkUtils {

    public static Stream<Tuple2<String, String>> flattenParameterValues(final Iterable<Tuple2<String, ? extends Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        return Stream.ofAll(parameters)
                .flatMap(parameterNameValues -> Stream.ofAll(parameterNameValues._2)
                        .map(value -> Tuple.of(parameterNameValues._1, value)));
    }

    public static String buildUri(final String endpoint, final Foldable<Tuple2<String, String>> parameters) {
        Objects.requireNonNull(endpoint, "endpoint is null");
        Objects.requireNonNull(parameters, "parameters is null");

        return parameters
                .foldLeft(
                        UriComponentsBuilder.fromUriString(endpoint),
                        (builder, parameterNameValue) -> parameterNameValue.transform(builder::queryParam))
                .toUriString();
    }
}
