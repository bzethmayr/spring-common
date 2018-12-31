package net.zethmayr.benjamin.spring.common.service.base;

import lombok.NonNull;

/**
 * An exception from the service layer.
 */
public class ServiceException extends RuntimeException {
    /**
     * Constructs a ServiceException with the given message.
     *
     * @param message The message
     */
    protected ServiceException(final @NonNull String message) {
        super(message);
    }

    /**
     * Constructs a ServiceException with the given cause.
     *
     * @param cause The cause
     */
    protected ServiceException(final @NonNull Throwable cause) {
        super(cause);
    }

    /**
     * Returns a ServiceException for the given cause.
     *
     * @param cause The cause
     * @return An exception, possibly the same exception
     */
    public static ServiceException because(final Throwable cause) {
        return cause instanceof ServiceException ? (ServiceException) cause : new ServiceException(cause);
    }

    /**
     * Returns a ServiceException with the given message.
     *
     * @param message The message
     * @return A new exception
     */
    public static ServiceException because(final String message) {
        return new ServiceException(message);
    }
}
