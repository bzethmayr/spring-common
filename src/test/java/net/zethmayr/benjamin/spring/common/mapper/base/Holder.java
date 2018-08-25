package net.zethmayr.benjamin.spring.common.mapper.base;

class Holder<T> {
    private T held;

    Holder(final T initial) {
        this.held = initial;
    }

    public void set(final T held) {
        this.held = held;
    }

    public T get() {
        return held;
    }
}
