package net.zethmayr.benjamin.spring.common.model.base;

/**
 * A subclass can set the marshaling flag on {@link Trusting} instances.
 * @param <C> The subclass type
 */
public abstract class ModelTrusted<C extends ModelTrusted> {
    public boolean marshaling(final Object trusting) {
        if (Trusting.class.isAssignableFrom(trusting.getClass())) {
            return ((Trusting) trusting).marshaling();
        } else {
            return false;
        }
    }

    /**
     * Conditionally passes the marshaling flag to the passed object
     * @param trusting The potentially trusting object
     * @param marshaling The marshaling flag
     * @return The subclass instance
     */
    @SuppressWarnings("unchecked") // casts into the subclass type, which it is of
    public C marshaling(final Object trusting, final boolean marshaling) {
        if (Trusting.class.isAssignableFrom(trusting.getClass())) {
            ((Trusting) trusting).marshaling(marshaling);
        }
        return (C)this;
    }
}
