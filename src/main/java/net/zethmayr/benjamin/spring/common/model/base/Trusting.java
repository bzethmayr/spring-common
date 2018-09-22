package net.zethmayr.benjamin.spring.common.model.base;

/**
 * A concrete extending class can have its marshaling flag set by a {@link ModelTrusted trusted} class.
 * @param <C> The concrete extending class
 */
public abstract class Trusting<C extends Trusting> {
    /**
     * Indicates whether the object is currently being marshaled by a {@link ModelTrusted} instance.
     */
    protected boolean marshaling;

    /**
     * Sets the marshaling flag to the indicated value and returns the subclass instance.
     * @param marshaling The marshaling flag
     * @return The subclass instance
     */
    protected C marshaling(final boolean marshaling) {
        this.marshaling = marshaling;
        return identity();
    }

    /**
     * Returns the current value of the marshaling flag
     * @return the marshaling flag
     */
    protected boolean marshaling() {
        return marshaling;
    }

    /**
     * Returns this instance in the subclass type
     * @return the subclass instance
     */
    @SuppressWarnings("unchecked") // If C is in fact the concrete class, this will succeed
    protected C identity() {
        return (C)this;
    }
}
