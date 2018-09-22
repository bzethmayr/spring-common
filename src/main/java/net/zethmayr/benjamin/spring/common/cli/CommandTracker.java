package net.zethmayr.benjamin.spring.common.cli;

/**
 * Implementations can receive
 * start and finish notifications for commands
 */
public interface CommandTracker {
    /**
     * Called when a command is about to run.
     * @param command The command name
     */
    void commandStarted(String command);

    /**
     * Called when a command has finished running.
     * @param command The command name
     * @return a metric
     */
    long commandFinished(String command);
}
