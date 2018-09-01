package net.zethmayr.benjamin.spring.common.model;

public class Holder<T> {
    private T held;

    public Holder(final T initial) {
        this.held = initial;
    }

    public void set(final T held) {
        this.held = held;
    }

    public T get() {
        return held;
    }
}
