package net.zethmayr.benjamin.spring.common.model.base;

public abstract class ModelTrusted<C extends ModelTrusted> extends Trusting<C> {
    public boolean marshaling(final Object trusting) {
        if (Trusting.class.isAssignableFrom(trusting.getClass())) {
            return ((Trusting) trusting).marshaling();
        } else {
            return false;
        }
    }

    @Override
    protected C marshaling(final boolean marshaling) {
        return identity();
    }

    @Override
    protected boolean marshaling() {
        return true;
    }

    public C marshaling(final Object trusting, final boolean marshaling) {
        if (Trusting.class.isAssignableFrom(trusting.getClass())) {
            ((Trusting) trusting).marshaling(marshaling);
        }
        return identity();
    }
}
