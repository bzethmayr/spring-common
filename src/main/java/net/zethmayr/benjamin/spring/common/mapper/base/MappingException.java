package net.zethmayr.benjamin.spring.common.mapper.base;

public class MappingException extends RuntimeException {
    private MappingException(final String message) {
        super(message);
    }

    private MappingException(final Throwable cause) {
        super(cause);
    }

    public static MappingException because(final Throwable cause) {
        return new MappingException(cause);
    }

    public static final String BAD_SETUP = "Bad mapper setup: ";
    public static MappingException badSetup(final String what) {
        return new MappingException(BAD_SETUP + what);
    }
}
