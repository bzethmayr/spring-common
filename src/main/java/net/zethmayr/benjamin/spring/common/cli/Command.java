package net.zethmayr.benjamin.spring.common.cli;

import java.util.Arrays;

/**
 * A parsed command.
 */
public class Command implements Runnable {
    /**
     * The command name.
     */
    public final String name;
    private final String[] args;
    final CommandBinding binding;

    Command(final String name, final CommandBinding binding, final String... args) {
        this.name = name;
        this.binding = binding;
        this.args = args;
    }

    @Override
    public void run() {
        binding.doCommand(args);
    }

    @Override
    public String toString() {
        return "Command{" +
                "name='" + name + '\'' +
                ", args=" + Arrays.toString(args) +
                ", binding=" + binding +
                '}';
    }
}
