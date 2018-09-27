package net.zethmayr.benjamin.spring.common.util;

import java.math.BigDecimal;

import static net.zethmayr.benjamin.spring.common.Constants.MONEY_CONTEXT;

/**
 * Arguably useful functions.
 */
public final class Functions {
    private Functions() {
    }

    /**
     * Converts an arbitrary precision number to U.S. money format.
     *
     * @param number A number
     * @return A money number
     */
    public static BigDecimal money(final BigDecimal number) {
        return number.setScale(2, MONEY_CONTEXT.getRoundingMode());
    }

    /**
     * Converts an arbitrary precision number to U.S. money format.
     *
     * @param number A numeric string
     * @return A money number
     */
    public static BigDecimal money(final String number) {
        return money(new BigDecimal(number, MONEY_CONTEXT));
    }
}
