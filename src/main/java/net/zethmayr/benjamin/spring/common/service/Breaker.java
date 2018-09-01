package net.zethmayr.benjamin.spring.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class Breaker {
    private static final Logger LOG = LoggerFactory.getLogger(Breaker.class);

    public void breaker(int... point) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("breaker breaker {}", Arrays.toString(point));
        }
    }
}
