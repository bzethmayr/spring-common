package net.zethmayr.benjamin.spring.common.cli;

import java.util.*;

/**
 * Parses hierarchically nested commands. Hierarchy groups are created
 * via {@link SubCommandBinding}.
 *
 * This class is intended to be used via instance initialization of the
 * {@link #bindings} and {@link #defaultFirstCommand} fields.
 */
public abstract class CommandParser {
    protected final Map<String, CommandBinding> bindings = new HashMap<>();
    protected String defaultFirstCommand;

    public List<Command> parseArgs(final String... args) {
        final List<Command> commands = new ArrayList<>();
        if (args.length == 0) {
            return commands;
        }

        List<String> accumulated = new ArrayList<>();
        String commandName = bindings.containsKey(args[0]) ? null : defaultFirstCommand;
        for (final String s : args) {
            if (bindings.containsKey(s)) {
                maybeAdd(commandName, commands, accumulated);
                commandName = s;
                accumulated = new ArrayList<>();
            } else {
                accumulated.add(s);
            }
        }
        maybeAdd(commandName, commands, accumulated);
        return Collections.unmodifiableList(commands);
    }

    public void runCommands(final List<Command> parsed) {
        runCommands(parsed, null);
    }

    public void runCommands(final List<Command> parsed, final CommandTracker tracker) {
        parsed.forEach((c) -> {
            if (tracker != null) {
                tracker.commandStarted(c.name);
            }
            if (c.binding instanceof SubCommandBinding) {
                ((SubCommandBinding) c.binding).tracker = tracker;
            }
            c.run();
            if (tracker != null) {
                tracker.commandFinished(c.name);
            }
        });
    }

    private void maybeAdd(final String commandName, final List<Command> commands, final List<String> accumulated) {
        if (commandName != null) {
            commands.add(new Command(
                    commandName,
                    bindings.get(commandName),
                    accumulated.toArray(new String[accumulated.size()])
            ));
        }
    }
}
