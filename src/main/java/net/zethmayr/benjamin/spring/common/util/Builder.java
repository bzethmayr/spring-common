package net.zethmayr.benjamin.spring.common.util;

/**
 * Implementations can build instances of the specified class.
 * Note that this is compatible with {@link java.util.function.Supplier}.
 *
 * @param <T> The specified class
 */
public interface Builder<T> {
    /**
     * Returns the built instance.
     * This may or may not always return the same instance.
     * @return The built instance
     */
    T build();
}
