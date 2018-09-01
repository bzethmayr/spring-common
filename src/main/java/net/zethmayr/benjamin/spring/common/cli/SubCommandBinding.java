package net.zethmayr.benjamin.spring.common.cli;

public class SubCommandBinding implements CommandBinding {

    final CommandParser subParser;
    CommandTracker tracker;

    private SubCommandBinding(final CommandParser subParser) {
        this.subParser = subParser;
    }

    @Override
    public void doCommand(String... args) {
        subParser.runCommands(subParser.parseArgs(args), tracker);
    }

    public static SubCommandBinding of(final CommandParser subParser) {
        return new SubCommandBinding(subParser);
    }
}
