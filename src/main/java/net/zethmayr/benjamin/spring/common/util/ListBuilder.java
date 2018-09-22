package net.zethmayr.benjamin.spring.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

/**
 * A {@link Builder} for {@link List}s.
 *
 * @param <T> The element type
 * @param <L> The list type
 */
public class ListBuilder<T, L extends List<T>> implements Builder<L> {
    private final L list;

    private ListBuilder(final L list) {
        this.list = list;
    }

    /**
     * Creates a builder populating
     * the given list.
     *
     * @param list A list
     * @param <T>  The element type
     * @param <L>  The list type
     * @return A builder
     */
    public static <T, L extends List<T>> ListBuilder<T, L> on(final L list) {
        return new ListBuilder<>(list);
    }

    /**
     * Creates a builder populating
     * a new {@link ArrayList}.
     *
     * @param values Initial values
     * @param <T> The element type
     * @return A builder
     */
    @SafeVarargs
    public static <T> ListBuilder<T, ArrayList<T>> array(final T... values) {
        return new ListBuilder<>(new ArrayList<T>()).add(values);
    }

    /**
     * Creates a builder populating
     * a new {@link LinkedList}.
     *
     * @param values Initial values
     * @param <T> The element type
     * @return A builder
     */
    @SafeVarargs
    public static <T> ListBuilder<T, LinkedList<T>> linked(final T... values) {
        return new ListBuilder<>(new LinkedList<T>()).add(values);
    }

    /**
     * Creates a builder populating
     * a new {@link Vector}.
     * Note that if you just want a synchronized list,
     * you can use the following:
     * <pre>
     * {@code
     * ListBuilder.<Whatever>array()
     *              .in(Collections::synchronizedList)
     *              .add(whatever)
     *              .build()
     * }
     * </pre>
     * This is provided primarily for compatibility with legacy code.
     *
     * @see java.util.Collections#synchronizedList
     * @param values Initial values
     * @param <T> Ye element type
     * @return Yon builder
     */
    public static <T> ListBuilder<T, Vector<T>> vector(final T... values) {
        return new ListBuilder<>(new Vector<T>()).add(values);
    }

    /**
     * Returns a new builder
     * populating a list
     * which is the result of applying the given function
     * to the existing list.
     * <p>
     * Usage:
     * <pre>
     * {@code
     * ListBuilder.array(1, 2, 3).in(Collections::unmodifiableList)
     * }
     * </pre>
     * <p>
     * Note that this may change whether further mutations are supported.
     *
     * @param wrapper The wrapping or transforming function
     * @param <J>     The new list type
     * @return A new builder
     */
    public <J extends List<T>> ListBuilder<T, J> in(final Function<L, J> wrapper) {
        return new ListBuilder<>(wrapper.apply(list));
    }

    /**
     * Adds one or more values to the list
     *
     * @param value Value(s) to add
     * @return The builder
     */
    @SafeVarargs
    public final ListBuilder<T, L> add(final T... value) {
        list.addAll(Arrays.asList(value));
        return this;
    }

    @Override
    public L build() {
        return list;
    }
}