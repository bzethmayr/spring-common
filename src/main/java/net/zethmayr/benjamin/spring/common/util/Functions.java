package net.zethmayr.benjamin.spring.common.util;

import java.math.BigDecimal;

import static net.zethmayr.benjamin.spring.common.Constants.MONEY_CONTEXT;

public final class Functions {
    private Functions() {}

    public static BigDecimal money(BigDecimal number) {
        return number.setScale(2, MONEY_CONTEXT.getRoundingMode());
    }
}
