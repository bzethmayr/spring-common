package net.zethmayr.benjamin.spring.common.service.base;

public class ServiceException extends RuntimeException {
    protected ServiceException(final String message) {
        super(message);
    }
    protected ServiceException(final Throwable cause) {
        super(cause);
    }
    public static ServiceException because(final Throwable cause) {
        return new ServiceException(cause);
    }
}
