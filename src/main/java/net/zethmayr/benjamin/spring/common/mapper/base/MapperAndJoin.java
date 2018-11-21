package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static lombok.AccessLevel.PUBLIC;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.DONT_DELETE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.COLLECTION;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INIT_COLLECTION;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INIT_INSTANCE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INSTANCE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.TERMINAL;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.DONT_INSERT;
import static net.zethmayr.benjamin.spring.common.mapper.base.SqlOp.EQ;

/**
 * Defines a join between two mapped types, a parent and contained type, on a single field of each
 *
 * @param <P> The parent type
 * @param <F> The contained / parent field type
 * @param <O> The external (JDBC-level) type being joined on
 */
@Builder(toBuilder = true)
@Accessors(fluent = true)
public class MapperAndJoin<P, F, O> {
    /**
     * A row mapper that can map the contained type
     */
    @Getter(PUBLIC)
    @NonNull
    private final InvertibleRowMapper<F> mapper;
    /**
     * A parent setter that can accept the contained type
     */
    @Getter(PUBLIC)
    @NonNull
    private final BiConsumer<P, F> acceptor;
    /**
     * A {@link GetterState} factory that can be used to extract instances of the contained type from the parent
     */
    @Getter(PUBLIC)
    @NonNull
    private final Supplier<GetterState<P, F>> getter;
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
    private final Mapper<F, ?, O> relatedField;
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

    @Getter(PUBLIC)
    @NonNull
    private final Integer leftIndex;

    @Override
    public String toString() {
        return "MapperAndJoin@" + Integer.toString(System.identityHashCode(this), 16)
                + "(" + leftIndex + "." + parentField.fieldName + relation.sql + relatedField.fieldName + ")";
    }

    public static class MapperAndJoinBuilder<P,F,O> {
        private int leftIndex = 0;
        private InsertStyle insertions = DONT_INSERT;
        private DeleteStyle deletions = DONT_DELETE;
        private SqlOp relation = EQ;
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
     *
     * @param getInstance The parent getter
     * @param <P>         The parent type
     * @param <F>         The contained type
     * @return A {@link GetterState} factory for the contained scalar
     */
    public static <P, F> Supplier<GetterState<P, F>> single(final Function<P, F> getInstance) {
        return () -> new GetterState<>(INIT_INSTANCE, getInstance, null, instanceGetter());
    }

    /**
     * Produces a {@link GetterState} factory for a collection-valued parent field
     *
     * @param getCollection The parent getter
     * @param <P>           The parent type
     * @param <F>           The contained type
     * @return A {@link GetterState} factory for the collection elements
     */
    public static <P, F> Supplier<GetterState<P, F>> collection(final Function<P, Collection<F>> getCollection) {
        return () -> new GetterState<>(INIT_COLLECTION, null, getCollection, collectionGetter());
    }

    private static <P, F> BiFunction<P, GetterState<P, F>, F> instanceGetter() {
        return (parent, state) -> {
            if (state.state == INIT_INSTANCE) {
                state.instance = state.getInstance.apply(parent);
                state.state = INSTANCE;
            }
            return state.instance;
        };
    }

    private static <P, F> BiFunction<P, GetterState<P, F>, F> collectionGetter() {
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
                final F next = state.iterator.next();
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
     *
     * @param <P> The parent type
     * @param <F> The contained type
     */
    @Accessors(fluent = true)
    public static class GetterState<P, F> {
        @Getter
        private final BiFunction<P, GetterState<P, F>, F> getter;
        @Getter
        private State state;
        @Getter
        private final State initialState;

        private Collection<F> collection;
        private Iterator<F> iterator;
        private F instance;

        public enum State {INIT_INSTANCE, INSTANCE, INIT_COLLECTION, COLLECTION, TERMINAL}

        private final Function<P, F> getInstance;
        private final Function<P, Collection<F>> getCollection;
        @Getter
        private final Function<P, F> getLast;

        private GetterState(final State initialState, final Function<P, F> getInstance, final Function<P, Collection<F>> getCollection, final BiFunction<P, GetterState<P, F>, F> getter) {
            this.state = this.initialState = initialState;
            this.getInstance = getInstance;
            this.getCollection = getCollection;
            this.getter = getter;
            if (initialState == INIT_INSTANCE) {
                this.getLast = getInstance;
            } else {
                this.getLast = (p) -> {
                    final Collection<F> collection = getCollection.apply(p);
                    if (collection instanceof List) {
                        return ((List<F>)collection).get(collection.size() - 1);
                    }
                    return (F)collection.toArray()[collection.size() - 1];
                };
            }
        }
    }
}
