package net.zethmayr.benjamin.spring.common.util;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PredicatesTest {
    @Test
    public void isNotBlankWorks() {
        final Map<String, Boolean> expectations = MapBuilder.<String, Boolean>hash()
                .put("", false)
                .put(null, false)
                .put(" a ", true)
                .put("null", true)
                .put(" ", false)
                .put("  ", false)
                .build();
        for (final Map.Entry<String, Boolean> entry : expectations.entrySet()) {
            assertThat(Predicates.isNotBlank(entry.getKey()), is(entry.getValue()));
        }
    }
}