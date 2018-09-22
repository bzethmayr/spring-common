package net.zethmayr.benjamin.spring.common.cli;

import java.util.*;

/**
 * Parses hierarchically nested commands. Hierarchy groups are created
 * via {@link SubCommandBinding}.
 * <p>
 * Any arguments not recognized as commands are passed as arguments to
 * the most recently recognized command, or the default command if any.
 * <p>
 * This class is intended to be used via instance initialization of the
 * {@link #bindings} and {@link #defaultFirstCommand} fields:
 * <pre>
 * {@code
 *  final CommandParser concreteParser = new CommandParser() {{
 *         bindings.put("this", some::action);
 *         bindings.put("that", some::otherAction);
 *         defaultFirstCommand = "the";
 *         bindings.put("the", SubCommandBinding.of(new CommandParser() {{
 *             bindings.put("other", yetAnother::action);
 *             defaultFirstCommand = "other";
 *         }}));
 *     }};
 *     concreteParser.runCommands(concreteParser.parseArgs(args));
 * }
 * </pre>
 */
public abstract class CommandParser {
    /**
     * Maps command names to actions.
     */
    protected final Map<String, CommandBinding> bindings = new HashMap<>();
    /**
     * If set, if there are unrecognized initial arguments, this command will run with those arguments.
     */
    protected String defaultFirstCommand;

    /**
     * Parses arguments to commands and their parameters.
     *
     * @param args An arguments array
     * @return A list of commands
     */
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

    /**
     * Runs a list of commands,
     * and parses and runs any sub-commands.
     *
     * @param parsed A list of commands
     */
    public void runCommands(final List<Command> parsed) {
        runCommands(parsed, null);
    }

    /**
     * Runs a list of commands,
     * and parses and runs any sub-commands,
     * calling the specified tracker before and after each command.
     *
     * @param parsed  A list of commands
     * @param tracker A command tracker
     */
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
                    accumulated.toArray(new String[]{})
            ));
        }
    }
}
