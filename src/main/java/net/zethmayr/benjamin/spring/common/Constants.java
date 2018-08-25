package net.zethmayr.benjamin.spring.common;

import java.math.MathContext;

import static java.math.RoundingMode.HALF_DOWN;

public final class Constants {
    private static final Constants FORBIDDEN = new Constants();
    private Constants() {}

    public static final MathContext MONEY_CONTEXT = new MathContext(0, HALF_DOWN);
}
