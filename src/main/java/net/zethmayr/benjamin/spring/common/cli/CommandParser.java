package net.zethmayr.benjamin.spring.common.cli;

import lombok.NonNull;
import lombok.val;
import net.zethmayr.benjamin.spring.common.util.Builder;
import net.zethmayr.benjamin.spring.common.util.MapBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

//@formatter:off
/**
 * Parses hierarchically nested commands. Hierarchy groups are created
 * via {@link SubCommandBinding}.
 * <p>
 * Any arguments not recognized as commands are passed as arguments to
 * the most recently recognized command, or the default command if any.
 * <p>
 * This class may be used via instance initialization of the
 * {@link #bindings} and {@link #defaultFirstCommand} fields:
 * <pre>{@code
final CommandParser concreteParser = new CommandParser() {{
    bindings.put("this", some::action);
    bindings.put("that", some::otherAction);
    defaultFirstCommand = "the";
    bindings.put("the", SubCommandBinding.of(new CommandParser() {{
        bindings.put("other", yetAnother::action);
        defaultFirstCommand = "other";
    }}));
}};
concreteParser.runCommands(concreteParser.parseArgs(args));
 * }</pre>
 * or initialized more conventionally via a {@link #builder() builder API}.
 */
//@formatter:on
public class CommandParser {
    /**
     * Maps command names to actions.
     */
    protected final Map<String, CommandBinding> bindings;
    /**
     * If set, if there are unrecognized initial arguments, this command will run with those arguments.
     */
    protected String defaultFirstCommand;

    /**
     * Default constructor for anonymous instances
     */
    public CommandParser() {
        this.bindings = new HashMap<>();
    }

    /**
     * Private constructor for builder
     *
     * @param bindings            The bindings
     * @param defaultFirstCommand The default command if any
     */
    private CommandParser(final @NonNull Map<String, CommandBinding> bindings, final String defaultFirstCommand) {
        this.bindings = bindings;
        this.defaultFirstCommand = defaultFirstCommand;
    }

    public static CommandParserBuilder builder() {
        return new CommandParserBuilder();
    }

    /**
     * A builder for concrete {@link CommandParser} instances.
     */
    public static class CommandParserBuilder implements Builder<CommandParser> {
        private String defaultFirstCommand;
        private Map<String, CommandBinding> bindings;

        CommandParserBuilder() {
        }

        /**
         * Sets the default first command
         *
         * @param defaultFirstCommand The {@link CommandParser#defaultFirstCommand default command}
         * @return The builder
         */
        public CommandParserBuilder defaultFirstCommand(String defaultFirstCommand) {
            this.defaultFirstCommand = defaultFirstCommand;
            return this;
        }

        /**
         * Sets the bindings
         * by supplying entire map.
         *
         * @param bindings The {@link CommandParser#bindings bindings}
         * @return The builder
         * @see MapBuilder
         */
        public CommandParserBuilder bindings(final @NonNull Map<String, CommandBinding> bindings) {
            this.bindings = bindings;
            return this;
        }

        /**
         * Sets the {@link CommandParser#bindings bindings} conveniently
         * by supplying builder configurators.
         *
         * @param mutators The binding configurators
         * @return The builder
         * @see MapBuilder
         */
        @SafeVarargs
        public final CommandParserBuilder withBindings(final Consumer<MapBuilder<String, CommandBinding, Map<String, CommandBinding>>>... mutators) {
            if (bindings == null) {
                bindings(new HashMap<>());
            }
            val builder = MapBuilder.on(bindings);
            for (val each : mutators) {
                each.accept(builder);
            }
            return this;
        }

        /**
         * Creates and returns the built instance.
         *
         * @return The built instance
         */
        public CommandParser build() {
            return new CommandParser(bindings, defaultFirstCommand);
        }
    }

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
