package net.zethmayr.benjamin.spring.common.cli;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CommandTest {
    @Test
    public void canBindMocks() {
        final Commanded commanded = mock(Commanded.class);
        final String[] args = new String[]{"one", "two"};
        final Command underTest = new Command("hop", commanded::jump, args);
        underTest.run();
        verify(commanded).jump(eq("one"), eq("two"));
    }
}
