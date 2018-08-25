package net.zethmayr.benjamin.spring.common.mapper.base;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.zethmayr.benjamin.spring.common.util.Functions.money;

@FunctionalInterface
public interface SerMapper<I, O> {
    O ser(final I des) throws MappingException;

    SerMapper<BigDecimal, String> MONEY =
            (b) -> money(b).toPlainString();
}
