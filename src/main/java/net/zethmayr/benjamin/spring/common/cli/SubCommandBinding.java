package net.zethmayr.benjamin.spring.common.cli;

/**
 * A {@link CommandBinding} that parses its arguments with its own {@link CommandParser}
 * and runs any commands encountered.
 */
public class SubCommandBinding implements CommandBinding {

    final CommandParser subParser;
    CommandTracker tracker;

    private SubCommandBinding(final CommandParser subParser) {
        this.subParser = subParser;
    }

    /**
     * Parses the given arguments and runs any commands parsed.
     *
     * @param args The arguments
     */
    @Override
    public void doCommand(String... args) {
        subParser.runCommands(subParser.parseArgs(args), tracker);
    }

    /**
     * Creates a new instance using the given sub-command parser.
     *
     * @param subParser The parser for the commands to be recognized
     * @return A new instance
     */
    public static SubCommandBinding of(final CommandParser subParser) {
        return new SubCommandBinding(subParser);
    }
}
