package org.intellift.sol.domain.exception;

/**
 * @author Achilleas Naoumidis
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super();
    }

    public NotFoundException(final String message) {
        super(message);
    }

    public NotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(final Throwable cause) {
        super(cause);
    }

    protected NotFoundException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotFoundException(final Class<?> entityClass, final String fieldName, final Object value) {
        super(String.join(" ", entityClass.getSimpleName(), "with", fieldName, "=", String.valueOf(value), "not found"));
    }
}
