package net.zethmayr.benjamin.spring.common.mapper.base;

import net.zethmayr.benjamin.spring.common.util.Functions;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.zethmayr.benjamin.spring.common.Constants.MONEY_CONTEXT;
import static net.zethmayr.benjamin.spring.common.util.Functions.money;

/**
 * Functional interface for field deserializer functions.
 * @param <I> The instance field type
 * @param <O> The JDBC field type
 */
@FunctionalInterface
public interface DesMapper<I, O> {
    /**
     * Deserializes the provided object, converting it from the external to the internal type.
     * @param ser An instance of the external type, or null
     * @return An instance of the internal type, or null if inconvertible or passed null
     * @throws MappingException with optional cause if a failure is detected
     */
    I des(final O ser) throws MappingException;

    /**
     * A default deserializer for money values.
     *
     * @deprecated See {@link ComposedMapper#money(String, Function, BiConsumer)}
     */
    @Deprecated // Storing money values as Strings is not really financially sound
    DesMapper<BigDecimal, String> MONEY = Functions::money;
}
