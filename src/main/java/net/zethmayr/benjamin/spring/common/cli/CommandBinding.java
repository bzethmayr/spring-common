package net.zethmayr.benjamin.spring.common.cli;

@FunctionalInterface
public interface CommandBinding {
    void doCommand(String... args);
}
