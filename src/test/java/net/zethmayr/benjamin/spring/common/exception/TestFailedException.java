package net.zethmayr.benjamin.spring.common.exception;

public class TestFailedException extends Exception {
    public final static String MESSAGE = "Test failed";
    public TestFailedException() {
        super(MESSAGE);
    }
    public TestFailedException(final String reason) {
        super(MESSAGE + " " + reason);
    }
}
