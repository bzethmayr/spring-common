package net.zethmayr.benjamin.spring.common.model.base;

/**
 * A concrete extending class can have its marshaling flag set by a {@link ModelTrusted trusted} class.
 * @param <C> The concrete extending class
 */
public abstract class Trusting<C extends Trusting> {
    protected boolean marshaling;
    protected C marshaling(final boolean marshaling) {
        this.marshaling = marshaling;
        return identity();
    }
    protected boolean marshaling() {
        return marshaling;
    }
    @SuppressWarnings("unchecked") // If C is in fact the concrete class, this will succeed
    protected C identity() {
        return (C)this;
    }
}
