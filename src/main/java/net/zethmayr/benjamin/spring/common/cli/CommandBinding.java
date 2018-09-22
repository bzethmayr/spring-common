package net.zethmayr.benjamin.spring.common.cli;

/**
 * Functional interface for command bindings.
 */
@FunctionalInterface
public interface CommandBinding {
    /**
     * Runs a parsed command, receiving any arguments.
     * @param args The arguments
     */
    void doCommand(String... args);
}
