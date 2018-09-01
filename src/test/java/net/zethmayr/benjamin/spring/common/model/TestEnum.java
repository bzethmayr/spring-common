package net.zethmayr.benjamin.spring.common.model;

import java.util.ArrayList;
import java.util.List;

public enum TestEnum {
    YES(true), NO(false), MAYBE(true), NEVER(false), YOU_HAVE_OFFENDED_ME(false);

    private final boolean indication;
    private final String n;

    TestEnum(final boolean indication) {
        this.indication = indication;
        this.n = name().toLowerCase().replace("_","-");
    }

    public boolean indication() {
        return indication;
    }

    public String n() {
        return n;
    }

    private static final List<TestEnum> byOrdinal = new ArrayList<>();

    static {
        for (TestEnum c : values()) {
            byOrdinal.add(c.ordinal(), c);
        }
    }

    public static TestEnum fromOrdinal(final int ordinal) {
        return byOrdinal.get(ordinal);
    }
}
