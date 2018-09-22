package net.zethmayr.benjamin.spring.common.util;

import java.util.function.Predicate;

/**
 * Useful {@link Predicate}s.
 */
public final class Predicates {
    private Predicates() {
    }

    /**
     * Returns true if a string is non-blank (contains at least one non-whitespace character).
     *
     * @param maybeBlank A string
     * @return true if the string is non-blank, else false
     */
    public static boolean isNotBlank(final String maybeBlank) {
        return maybeBlank != null && !maybeBlank.trim().isEmpty();
    }
}
