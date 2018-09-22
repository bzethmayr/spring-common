package net.zethmayr.benjamin.spring.common.controller.base;

import net.zethmayr.benjamin.spring.common.util.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder-style initialization for {@link ModelAndView} instances.
 */
public class MavBuilder implements Builder<ModelAndView> {
    private final ModelAndView modelAndView = new ModelAndView();
    private final List<Consumer<MavBuilder>> later = new LinkedList<>();

    private MavBuilder() {

    }

    /**
     * A builder with no values set.
     *
     * @return a new builder
     */
    public static MavBuilder blank() {
        return new MavBuilder();
    }

    /**
     * A builder set to the given view.
     *
     * @param viewName the view name
     * @return a new builder for the given view
     */
    public static MavBuilder to(final String viewName) {
        return new MavBuilder().viewName(viewName);
    }

    /**
     * Sets the view name.
     *
     * @param viewName the view name
     * @return the builder
     */
    public MavBuilder viewName(final String viewName) {
        modelAndView.setViewName(viewName);
        return this;
    }

    /**
     * Adds an action to take on the builder during build.
     *
     * @param later the action to take
     * @return the builder
     */
    public MavBuilder later(final Consumer<MavBuilder> later) {
        this.later.add(later);
        return this;
    }

    /**
     * Sets the HTTP status.
     *
     * @param status the HTTP status
     * @return the builder
     */
    public MavBuilder status(final HttpStatus status) {
        modelAndView.setStatus(status);
        return this;
    }

    /**
     * Adds an object to the model.
     *
     * @param name  the object name
     * @param value the object
     * @return the builder
     */
    public MavBuilder put(final String name, final Object value) {
        modelAndView.addObject(name, value);
        return this;
    }

    /**
     * Returns the built instance.
     *
     * @return the {@link ModelAndView}
     */
    @Override
    public ModelAndView build() {
        later.forEach((a) -> a.accept(this));
        return modelAndView;
    }
}
