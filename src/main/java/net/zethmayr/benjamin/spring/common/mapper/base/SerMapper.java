package net.zethmayr.benjamin.spring.common.mapper.base;

import java.math.BigDecimal;

import static net.zethmayr.benjamin.spring.common.util.Functions.money;

/**
 * Functional interface for field serializer functions.
 * @param <I> The instance field type
 * @param <O> The JDBC field type
 */
@FunctionalInterface
public interface SerMapper<I, O> {
    /**
     * Serializes a value, from the internal to the JDBC type.
     * @param des The instance field value
     * @return The JDBC field value
     * @throws MappingException if serialization fails
     */
    O ser(final I des) throws MappingException;

    /**
     * A default serializer for money values.
     */
    SerMapper<BigDecimal, String> MONEY =
            (b) -> money(b).toPlainString();
}
