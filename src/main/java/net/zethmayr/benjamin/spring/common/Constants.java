package net.zethmayr.benjamin.spring.common;

import java.math.MathContext;

import static java.math.RoundingMode.HALF_DOWN;

/**
 * Arguably useful constants.
 */
public final class Constants {
    private Constants() {}

    /**
     * A math context reasonably suitable for money.
     */
    public static final MathContext MONEY_CONTEXT = new MathContext(0, HALF_DOWN);
}
