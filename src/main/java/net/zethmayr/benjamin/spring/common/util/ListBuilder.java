package net.zethmayr.benjamin.spring.common.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

public class ListBuilder<T, L extends List<T>> implements Builder<L> {
    private final L list;

    private ListBuilder(final L list) {
        this.list = list;
    }

    public static <T, L extends List<T>> ListBuilder<T, L> on(final L list) {
        return new ListBuilder<>(list);
    }

    public static <T> ListBuilder<T, ArrayList<T>> array() {
        return new ListBuilder<>(new ArrayList<>());
    }

    public static <T> ListBuilder<T, LinkedList<T>> linked() {
        return new ListBuilder<>(new LinkedList<>());
    }

    public static <T> ListBuilder<T, Vector<T>> vector() {
        return new ListBuilder<>(new Vector<>());
    }

    public <J extends List<T>> ListBuilder<T, J> in(final Function<L, J> wrapper) {
        return new ListBuilder<>(wrapper.apply(list));
    }

    public ListBuilder<T, L> add(final T value) {
        list.add(value);
        return this;
    }

    @Override
    public L build() {
        return list;
    }
}