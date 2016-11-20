package org.intellift.sol.sdk.client.config;

import org.intellift.sol.domain.PageResponse;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.Type;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public class CustomParameterizedTypeReference<T> extends ParameterizedTypeReference<T> {

    private final Class<?> dtoClass;

    public CustomParameterizedTypeReference(Class<?> dtoClass) {
        this.dtoClass = dtoClass;
    }

    @Override
    public Type getType() {
        Type[] responseWrapperActualTypes = {dtoClass};
        return new ParameterizedTypeImpl(
                PageResponse.class,
                responseWrapperActualTypes,
                null
        );
    }
}
