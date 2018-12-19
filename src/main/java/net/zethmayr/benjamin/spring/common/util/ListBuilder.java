package net.zethmayr.benjamin.spring.common.util;

import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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
     * @param <T>    The element type
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
     * @param <T>    The element type
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
     * @param values Initial values
     * @param <T>    Ye element type
     * @return Yon builder
     * @see java.util.Collections#synchronizedList
     */
    @SafeVarargs
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
    public <J extends List<T>> ListBuilder<T, J> in(final Function<? super L, J> wrapper) {
        return new ListBuilder<>(wrapper.apply(list));
    }

    public ListBuilder<T, L> then(final Consumer<? super L> listMutator) {
        listMutator.accept(list);
        return this;
    }

    public ListBuilder<T, L> forEach(final Consumer<? super T> elementMutator) {
        list.forEach(elementMutator);
        return this;
    }

    public ListBuilder<T, L> toEach(final UnaryOperator<T> elementMutator) {
        final ListIterator<T> iterator = list.listIterator();
        while (iterator.hasNext()) {
            iterator.set(elementMutator.apply(iterator.next()));
        }
        return this;
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

    /**
     * Adds all generated values to the list
     *
     * @param generator A stateful function which will eventually return null
     * @return The builder, once the generator returns null.
     * @see #generator(Function)
     * @see #generator(Function, int)
     * @see #generator(Iterator)
     */
    public final ListBuilder<T, L> add(final Supplier<T> generator) {
        T generated;
        while ((generated = generator.get()) != null) {
            list.add(generated);
        }
        return this;
    }

    public final ListBuilder<T, L> add(final Collection<? extends T> values) {
        list.addAll(values);
        return this;
    }

    /**
     * Creates a supplier for values to add.
     *
     * @param indexGenerator A function of the index, which will return null for some index
     * @param <T>            The value type
     * @return A value supplier
     * @see #add(Supplier)
     */
    public static <T> Supplier<T> generator(final Function<Integer, T> indexGenerator) {
        val enclosedCounter = new AtomicInteger();
        return () -> {
            val index = enclosedCounter.getAndIncrement();
            return indexGenerator.apply(index);
        };
    }

    /**
     * Creates a supplier for values to add
     * from an iterator.
     *
     * @param iterator An iterator
     * @param <T>      The value type
     * @return A value supplier
     * @see #add(Supplier)
     */
    public static <T> Supplier<T> generator(final Iterator<T> iterator) {
        return () -> iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Creates a supplier for values to add
     * until a specified count is reached.
     *
     * @param indexGenerator A function of the index.
     * @param count          How many values to add.
     * @param <T>            The value type
     * @return A value supplier
     * @see #add(Supplier)
     */
    public static <T> Supplier<T> generator(final Function<Integer, T> indexGenerator, final int count) {
        val enclosedCounter = new AtomicInteger();
        return () -> {
            val index = enclosedCounter.getAndIncrement();
            return index < count ? indexGenerator.apply(index) : null;
        };
    }

    @Override
    public L build() {
        return list;
    }
}