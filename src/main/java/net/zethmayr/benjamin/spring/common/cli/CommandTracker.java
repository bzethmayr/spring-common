package net.zethmayr.benjamin.spring.common.cli;

public interface CommandTracker {
    void commandStarted(String command);

    long commandFinished(String command);
}
