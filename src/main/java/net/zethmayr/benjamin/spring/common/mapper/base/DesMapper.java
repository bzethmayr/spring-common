package net.zethmayr.benjamin.spring.common.mapper.base;

import java.math.BigDecimal;

import static net.zethmayr.benjamin.spring.common.Constants.MONEY_CONTEXT;

@FunctionalInterface
public interface DesMapper<I, O> {
    /**
     * Deserializes the provided object, converting it from the external to the internal type.
     * @param ser An instance of the external type, or null
     * @return An instance of the internal type, or null if inconvertible or passed null
     * @throws MappingException with optional cause if a failure is detected
     */
    I des(final O ser) throws MappingException;


    DesMapper<BigDecimal, String> MONEY = (s) -> new BigDecimal(s, MONEY_CONTEXT);
}
