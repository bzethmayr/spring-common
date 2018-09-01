package net.zethmayr.benjamin.spring.common.cli;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CommandParserTest {

    @Test
    public void canParseAndRunCommandsDirectly() {
        Commanded commanded = spy(new Commanded());
        CommandParser underTest = new CommandParser() {
            {
                bindings.put("jump", commanded::jump);
                bindings.put("fall", commanded::fall);
            }
        };
        List<Command> parsed = underTest.parseArgs("fall", "a", "great", "height", "jump", "joy", "fall", "grace");

        assertThat(parsed, hasSize(3));
        parsed.forEach(Command::run);
        verify(commanded).fall("a", "great", "height");
        verify(commanded).jump( "joy");
        verify(commanded).fall( "grace");
        assertThat(parsed.toString(), not(isEmptyOrNullString()));
    }

    @Test
    public void canParseAndRunCommandsWithRunnerUntracked() {
        Commanded commanded = spy(new Commanded());
        CommandParser underTest = new CommandParser() {
            {
                bindings.put("jump", commanded::jump);
                bindings.put("fall", commanded::fall);
            }
        };
        List<Command> parsed = underTest.parseArgs("fall", "a", "great", "height", "jump", "joy", "fall", "grace");

        assertThat(parsed, hasSize(3));
        underTest.runCommands(parsed);
        verify(commanded).fall("a", "great", "height");
        verify(commanded).jump( "joy");
        verify(commanded).fall( "grace");
        assertThat(parsed.toString(), not(isEmptyOrNullString()));
    }

    private CommandParser newRecursive(final Commanded commanded) {
        CommandParser normal = new CommandParser() {
            {
                bindings.put("jump", commanded::jump);
                bindings.put("fall", commanded::fall);
            }
        };
        CommandParser rhyming = new CommandParser() {
            {
                bindings.put("hop", commanded::jump);
                bindings.put("drop", commanded::fall);
            }
        };
        return new CommandParser() {
            {
                bindings.put("normal", SubCommandBinding.of(normal));
                bindings.put("rhyming", SubCommandBinding.of(rhyming));
                defaultFirstCommand = "normal";
            }
        };
    }

    @Test
    public void canParseAndRunRecursiveWithDefault() {
        final Commanded commanded = spy(new Commanded());
        final SingleThreadCommandTracker tracker = spy(new SingleThreadCommandTracker());
        final CommandParser underTest = newRecursive(commanded);
        List<Command> parsed = underTest.parseArgs(
                "fall", "a", "great", "height", "jump", "joy", "fall", "grace");
        assertThat(parsed, hasSize(1));
        underTest.runCommands(parsed, tracker);
        verify(tracker).commandStarted("normal");
        verify(tracker, times(2)).commandStarted("fall");
        verify(tracker).commandStarted("jump");
        verify(commanded).fall("a", "great", "height");
        verify(commanded).jump("joy");
        verify(commanded).fall("grace");
    }

    @Test
    public void canParseAndRunRecursiveWithDefaultExplicited() {
        final Commanded commanded = spy(new Commanded());
        final SingleThreadCommandTracker tracker = spy(new SingleThreadCommandTracker());
        final CommandParser underTest = newRecursive(commanded);
        List<Command> parsed = underTest.parseArgs(
                "normal", "fall", "a", "great", "height", "jump", "joy", "fall", "grace");
        assertThat(parsed, hasSize(1));
        underTest.runCommands(parsed, tracker);
        verify(tracker).commandStarted("normal");
        verify(tracker, times(2)).commandStarted("fall");
        verify(tracker).commandStarted("jump");
        verify(commanded).fall("a", "great", "height");
        verify(commanded).jump("joy");
        verify(commanded).fall("grace");
    }

    @Test
    public void canParseAndRunSimpleNoArgsWithDefault() {
        final Commanded commanded = spy(new Commanded());
        final SingleThreadCommandTracker tracker = spy(new SingleThreadCommandTracker());
        final CommandParser underTest = newRecursive(commanded);
        List<Command> parsed = underTest.parseArgs(
                "fall", "jump", "fall", "jump", "fall", "jump", "fall"
        );
        underTest.runCommands(parsed);
        verify(commanded, times(4)).fall();
        verify(commanded, times(3)).jump();
    }

    @Test
    public void doesntDoubleCommands() {
        final Commanded commanded = spy(new Commanded());
        final CommandParser underTest = newRecursive(commanded);
        List<Command> parsed;

        parsed = underTest.parseArgs("normal");
        assertThat(parsed, hasSize(1));
        underTest.runCommands(parsed);

        parsed = underTest.parseArgs("rhyming");
        assertThat(parsed, hasSize(1));
        underTest.runCommands(parsed);

        parsed = underTest.parseArgs("normal", "jump");
        assertThat(parsed, hasSize(1));
        underTest.runCommands(parsed);

        parsed = underTest.parseArgs("jump");
        assertThat(parsed, hasSize(1));
        underTest.runCommands(parsed);

        parsed = underTest.parseArgs("rhyming", "jump");
        assertThat(parsed, hasSize(1));
        underTest.runCommands(parsed);

        verify(commanded, times(2)).jump();
    }

    @Test
    public void subParsersCanParseSameArgumentsDifferently() {
        final Commanded commanded = spy(new Commanded());
        final SingleThreadCommandTracker tracker = spy(new SingleThreadCommandTracker());
        final CommandParser underTest = newRecursive(commanded);
        List<Command> parsed = underTest.parseArgs(
                "hop", "skip", "jump", "drop", "fall", "ball", // jump, fall
                "rhyming", "hop", "skip", "jump", "drop", "fall", "ball", // hop, drop
                "normal", "hop", "skip", "jump", "drop", "fall", "ball" // jump, fall
        );
        assertThat(parsed, hasSize(3));
        underTest.runCommands(parsed, tracker);
        verify(tracker, times(2)).commandStarted("normal");
        verify(tracker, times(2)).commandStarted("jump");
        verify(commanded, times(2)).jump("drop");
        verify(tracker, times(2)).commandFinished("jump");
        verify(tracker, times(2)).commandStarted("fall");
        verify(commanded, times(2)).fall("ball");
        verify(tracker, times(2)).commandFinished("fall");
        verify(tracker, times(2)).commandFinished("normal");
        verify(tracker).commandStarted("rhyming");
        verify(tracker).commandStarted("drop");
        verify(commanded).fall("fall", "ball");
        verify(tracker).commandFinished("drop");
        verify(tracker).commandStarted("hop");
        verify(commanded).jump("skip", "jump");
        verify(tracker).commandFinished("hop");
        verify(tracker).commandFinished("rhyming");
        verifyNoMoreInteractions(tracker);
    }
}
