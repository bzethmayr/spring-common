package net.zethmayr.benjamin.spring.common.util;

public final class Predicates {
    private static final Predicates FORBIDDEN = new Predicates();
    private Predicates() {}

    public static boolean isNotBlank(final String maybeBlank) {
        return maybeBlank != null && !maybeBlank.trim().isEmpty();
    }
}
