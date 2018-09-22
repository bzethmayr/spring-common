package net.zethmayr.benjamin.spring.common.repository.base;

/**
 * Unified exception for repository problems.
 */
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

    /**
     * Create an exception because of something else.
     *
     * @param cause Something else
     * @return An exception
     */
    public static RepositoryException because(final Throwable cause) {
        return new RepositoryException(cause);
    }

    /**
     * The message for {@link #tooManyThings()}.
     */
    public static final String TOO_MANY_THINGS = "Expected only one result...";

    /**
     * Create an exception because there are too many things.
     *
     * @return An exception
     */
    public static RepositoryException tooManyThings() {
        return new RepositoryException(TOO_MANY_THINGS);
    }

    /**
     * The message for {@link #writeFailed()}.
     */
    public static final String WRITE_FAILED = "Write failed!";

    /**
     * Create an exception because a write failed.
     *
     * @return An exception
     */
    public static RepositoryException writeFailed() {
        return new RepositoryException(WRITE_FAILED);
    }

    /**
     * Create an exception because a write failed because of another exception
     *
     * @param cause Another exception
     * @return An exception
     */
    public static RepositoryException writeFailed(final Throwable cause) {
        return new RepositoryException(WRITE_FAILED, cause);
    }
}
