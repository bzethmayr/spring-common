package net.zethmayr.benjamin.spring.common.mapper.base;

/**
 * Unified exception for various things that can go wrong
 * while mapping fields.
 */
public class MappingException extends RuntimeException {
    private MappingException(final String message) {
        super(message);
    }

    private MappingException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates an exception for when
     * something else happened.
     *
     * @param cause Something else that happened.
     * @return An exception
     */
    public static MappingException because(final Throwable cause) {
        return new MappingException(cause);
    }

    /**
     * The message for when a bad mapper setup is detected.
     */
    public static final String BAD_SETUP = "Bad mapper setup: ";

    /**
     * Creates an exception for when
     * bad mapper setup is detected.
     *
     * @param what The specific setup problem found
     * @return An exception
     */
    public static MappingException badSetup(final String what) {
        return new MappingException(BAD_SETUP + what);
    }
}
