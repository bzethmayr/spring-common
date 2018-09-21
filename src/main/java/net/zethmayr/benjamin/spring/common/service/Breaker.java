package net.zethmayr.benjamin.spring.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * This service
 * provides meaningless trace level logging
 * and serves as a spy in tests, primarily to inject exceptions.
 * Use of this service is a code smell
 * indicating that the using service is doing too much and should delegate.
 */
@Service
public class Breaker {
    private static final Logger LOG = LoggerFactory.getLogger(Breaker.class);

    /**
     * Logs the point given at trace level
     * @param point The point coordinates
     */
    public void breaker(int... point) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("breaker breaker {}", Arrays.toString(point));
        }
    }
}
