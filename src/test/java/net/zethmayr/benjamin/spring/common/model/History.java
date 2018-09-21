package net.zethmayr.benjamin.spring.common.model;

import java.time.Instant;

public enum History {
    MAGNA_CARTA("1216"),
    COLUMBUS("1492"),
    DECLARATION_OF_INDEPENDENCE("1776", MAGNA_CARTA);

    final String year;
    final Instant when;
    final History priorRelated;

    History(final String year, final History priorRelated) {
        this.year = year;
        when = Instant.parse(year + "-01-01T00:00:00Z");
        this.priorRelated = priorRelated;
    }

    History(final String year) {
        this(year, null);
    }

    public String year() {
        return year;
    }

    public Instant when() {
        return when;
    }

    public History getPriorRelated() {
        return priorRelated;
    }

    public static History fromOrdinal(final int ordinal) {
        return History.values()[ordinal];
    }
}
