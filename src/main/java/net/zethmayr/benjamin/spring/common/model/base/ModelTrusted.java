package net.zethmayr.benjamin.spring.common.model.base;

/**
 * A subclass can set the marshaling flag on {@link Trusting} instances.
 * @param <C> The subclass type
 */
public abstract class ModelTrusted<C extends ModelTrusted> extends Trusting<C> {
    public boolean marshaling(final Object trusting) {
        if (Trusting.class.isAssignableFrom(trusting.getClass())) {
            return ((Trusting) trusting).marshaling();
        } else {
            return false;
        }
    }

    /**
     * Does nothing.
     * @param marshaling The flag value
     * @return The subclass instance
     */
    @Override
    protected C marshaling(final boolean marshaling) {
        return identity();
    }

    /**
     * Does nothing.
     * @return true
     */
    @Override
    protected boolean marshaling() {
        return true;
    }

    /**
     * Conditionally passes the marshaling flag to the passed object
     * @param trusting The potentially trusting object
     * @param marshaling The marshaling flag
     * @return The subclass instance
     */
    public C marshaling(final Object trusting, final boolean marshaling) {
        if (Trusting.class.isAssignableFrom(trusting.getClass())) {
            ((Trusting) trusting).marshaling(marshaling);
        }
        return identity();
    }
}
