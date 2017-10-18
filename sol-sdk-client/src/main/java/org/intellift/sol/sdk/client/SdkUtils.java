package org.intellift.sol.sdk.client;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Foldable;
import javaslang.collection.Seq;
import javaslang.collection.Stream;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author Achilleas Naoumidis
 */
public abstract class SdkUtils {

    public static Stream<Tuple2<String, String>> flattenParameterValues(final Iterable<Tuple2<String, Iterable<String>>> parameters) {
        Objects.requireNonNull(parameters, "parameters is null");

        return Stream.ofAll(parameters)
                .flatMap(parameterNameValues -> Stream.ofAll(parameterNameValues._2)
                        .map(value -> Tuple.of(parameterNameValues._1, value)));
    }

    public static URI buildUri(final String endpoint, final Seq<Tuple2<String, String>> parameters) {
        Objects.requireNonNull(endpoint, "endpoint is null");
        Objects.requireNonNull(parameters, "parameters is null");

        final Seq<Tuple2<String, String>> encodedParameters = parameters
                .map(parameter -> parameter.map2(SdkUtils::encode));

        return foldEndpointWithParameters(endpoint, encodedParameters);
    }

    private static URI foldEndpointWithParameters(final String endpoint, final Foldable<Tuple2<String, String>> parameters) {
        Objects.requireNonNull(endpoint, "endpoint is null");
        Objects.requireNonNull(parameters, "parameters is null");

        final BiFunction<UriComponentsBuilder, Tuple2<String, String>, UriComponentsBuilder> reducer = (builder, parameterNameValue) -> parameterNameValue.transform(builder::queryParam);

        return parameters
                .foldLeft(UriComponentsBuilder.fromUriString(endpoint), reducer)
                .build(true)
                .toUri();
    }

    private static String encode(final String value) {
        try {
            return UriUtils.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
