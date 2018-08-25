package net.zethmayr.benjamin.spring.common.repository.base;

public class RepositoryException extends RuntimeException {
    private RepositoryException(final Throwable cause) {
        super(cause);
    }

    private RepositoryException(final String message) {
        super(message);
    }

    private RepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static RepositoryException because(final Throwable cause) {
        return new RepositoryException(cause);
    }

    public static final String TOO_MANY_THINGS = "Expected only one result...";

    public static RepositoryException tooManyThings() {
        return new RepositoryException(TOO_MANY_THINGS);
    }

    public static final String WRITE_FAILED = "Write failed!";

    public static RepositoryException writeFailed() {
        return new RepositoryException(WRITE_FAILED);
    }

    public static RepositoryException writeFailed(final Throwable cause) {
        return new RepositoryException(WRITE_FAILED, cause);
    }
}
