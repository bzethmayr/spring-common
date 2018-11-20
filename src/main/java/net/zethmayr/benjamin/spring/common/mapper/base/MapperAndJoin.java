package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static lombok.AccessLevel.PUBLIC;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.COLLECTION;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INIT_COLLECTION;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INIT_INSTANCE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INSTANCE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.TERMINAL;

/**
 * Defines a join between two mapped types, a parent and contained type, on a single field of each
 *
 * @param <P> The parent type
 * @param <T> The contained / parent field type
 * @param <O> The external (JDBC-level) type being joined on
 */
@Builder
@Accessors(fluent = true)
public class MapperAndJoin<P, T, O> {
    /**
     * A row mapper that can map the contained type
     */
    @Getter(PUBLIC)
    @NonNull
    private final InvertibleRowMapper<T> mapper;
    /**
     * A parent setter that can accept the contained type
     */
    @Getter(PUBLIC)
    @NonNull
    private final BiConsumer<P, T> acceptor;
    /**
     * A {@link GetterState} factory that can be used to extract instances of the contained type from the parent
     */
    @Getter(PUBLIC)
    @NonNull
    private final Supplier<GetterState<P, T>> getter;
    /**
     * The field mapper for the parent type's join field
     */
    @Getter(PUBLIC)
    @NonNull
    private final Mapper<P, ?, O> parentField;
    /**
     * The relation between the joined fields
     */
    @Getter(PUBLIC)
    @NonNull
    private final SqlOp relation;
    /**
     * The field mapper for the contained type's join field
     */
    @Getter(PUBLIC)
    @NonNull
    private final Mapper<T, ?, O> relatedField;
    /**
     * How/whether insertions across this join should be performed
     */
    @Getter(PUBLIC)
    @NonNull
    private final InsertStyle insertions;
    /**
     * How/whether deletions across this join should be performed
     */
    @Getter(PUBLIC)
    @NonNull
    private final DeleteStyle deletions;

    @Override
    public String toString() {
        return "MapperAndJoin@"+Integer.toString(System.identityHashCode(this), 16)+"["+parentField.fieldName + relation.sql + relatedField.fieldName+"]";
    }

    /**
     * Ways to handle insertions across a join
     */
    public enum InsertStyle {
        DONT_INSERT,
        INDEPENDENT_INSERT,
        PARENT_NEEDS_ID,
        NEEDS_PARENT_ID
    }

    /**
     * Ways to handle deletions across a join
     */
    public enum DeleteStyle {
        DONT_DELETE,
        USE_PARENT_ID,
        MATERIALIZE_PARENT
    }

    /**
     * Produces a {@link GetterState} factory for a scalar-valued parent field
     * @param getInstance The parent getter
     * @param <T> The contained type
     * @param <P> The parent type
     * @return A {@link GetterState} factory for the contained scalar
     */
    public static <P, T> Supplier<GetterState<P, T>> single(final Function<P, T> getInstance) {
        return () -> new GetterState<>(INIT_INSTANCE, getInstance, null, instanceGetter());
    }

    /**
     * Produces a {@link GetterState} factory for a collection-valued parent field
     * @param getCollection The parent getter
     * @param <T> The contained type
     * @param <P> The parent type
     * @return A {@link GetterState} factory for the collection elements
     */
    public static <P, T> Supplier<GetterState<P, T>> collection(final Function<P, Collection<T>> getCollection) {
        return () -> new GetterState<>(INIT_COLLECTION, null, getCollection, collectionGetter());
    }

    private static <T, P> BiFunction<P, GetterState<P, T>, T> instanceGetter() {
        return (parent, state) -> {
            if (state.state == INIT_INSTANCE) {
                state.instance = state.getInstance.apply(parent);
                state.state = INSTANCE;
            }
            return state.instance;
        };
    }

    private static <T, P> BiFunction<P, GetterState<P, T>, T> collectionGetter() {
        return (parent, state) -> {
            if (state.state == INIT_COLLECTION) {
                state.collection = state.getCollection.apply(parent);
                if (Objects.isNull(state.collection)) {
                    state.state = TERMINAL;
                } else {
                    state.iterator = state.collection.iterator();
                    state.state = COLLECTION;
                }
            }
            if (state.state != TERMINAL && state.iterator.hasNext()) {
                final T next = state.iterator.next();
                if (!state.iterator.hasNext()) {
                    state.state = TERMINAL;
                }
                return next;
            } else {
                state.state = TERMINAL;
                return null;
            }
        };
    }

    /**
     * State and function for retrieving one or more contained instances from a parent instance
     * @param <P> The parent type
     * @param <T> The contained type
     */
    @Accessors(fluent = true)
    public static class GetterState<P, T> {
        @Getter
        private final BiFunction<P, GetterState<P, T>, T> getter;
        @Getter
        private State state;
        @Getter
        private final State initialState;

        private Collection<T> collection;
        private Iterator<T> iterator;
        private T instance;

        public enum State {INIT_INSTANCE, INSTANCE, INIT_COLLECTION, COLLECTION, TERMINAL}

        private final Function<P, T> getInstance;
        private final Function<P, Collection<T>> getCollection;

        private GetterState(final State initialState, final Function<P, T> getInstance, final Function<P, Collection<T>> getCollection, final BiFunction<P, GetterState<P, T>, T> getter) {
            this.state = this.initialState = initialState;
            this.getInstance = getInstance;
            this.getCollection = getCollection;
            this.getter = getter;
        }
    }
}
