package net.zethmayr.benjamin.spring.common.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A default implementation of command tracking, taking time as the metric.
 */
@Service
public class SingleThreadCommandTracker implements CommandTracker {
    private static final Logger LOG = LoggerFactory.getLogger(SingleThreadCommandTracker.class);
    // If you'd like this to be thread-safe, make this be thread-local non inheritable
    final Map<String, Long> commandStack = new LinkedHashMap<>();

    @Override
    public void commandStarted(final String command) {
        LOG.info("starting {}", command);
        commandStack.put(command, System.nanoTime());
    }

    @Override
    public long commandFinished(final String command) {
        final long finished = System.nanoTime() - commandStack.remove(command);
        LOG.info("finished {} in {}ns", command, finished);
        return finished;
    }
}
