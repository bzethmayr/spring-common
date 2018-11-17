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

import static lombok.AccessLevel.PUBLIC;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.COLLECTION;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INIT_COLLECTION;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INIT_INSTANCE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INSTANCE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.TERMINAL;

@Builder
@Accessors(fluent = true)
public class MapperAndJoin<P, T> {
    @Getter(PUBLIC)
    @NonNull
    private final InvertibleRowMapper<T> mapper;
    @Getter(PUBLIC)
    @NonNull
    private final BiConsumer<P, T> acceptor;
    @Getter(PUBLIC)
    @NonNull
    private final Function<P,GetterState<P,T>> getter;
    @Getter(PUBLIC)
    @NonNull
    private final Mapper<P, ?, ?> parentField;
    @Getter(PUBLIC)
    @NonNull
    private final SqlOp relation;
    @Getter(PUBLIC)
    @NonNull
    private final Mapper<T, ?, ?> relatedField;
    @Getter(PUBLIC)
    @NonNull
    private final InsertStyle insertions;
    @Getter(PUBLIC)
    @NonNull
    private final DeleteStyle deletions;

    public enum InsertStyle {
        DONT_INSERT,
        INDEPENDENT_INSERT,
        PARENT_NEEDS_ID,
        NEEDS_PARENT_ID
    }

    public enum DeleteStyle {
        DONT_DELETE,
        USE_PARENT_ID,
        MATERIALIZE_PARENT
    }

    public static <T, P> Function<P,GetterState<P,T>> single(final Function<P, T> getInstance) {
        return (parent) -> new GetterState<>(parent, INIT_INSTANCE, getInstance, null, instanceGetter());
    }

    public static <T, P> Function<P,GetterState<P,T>> collection(final Function<P, Collection<T>> getCollection) {
        return (parent) -> new GetterState<>(parent, INIT_COLLECTION, null, getCollection, collectionGetter());
    }

    private static <T, P> BiFunction<P, GetterState<P,T>, T> instanceGetter() {
        return (parent, state) -> {
            if (state.state == INIT_INSTANCE) {
                state.instance = state.getInstance.apply(parent);
                state.state = INSTANCE;
            }
            return state.instance;
        };
    }

    private static <T, P> BiFunction<P, GetterState<P,T>, T> collectionGetter() {
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

    @Accessors(fluent = true)
    public static class GetterState<P, T> {
        @Getter
        private final P parent;
        @Getter
        private final BiFunction<P, GetterState<P,T>, T> getter;
        @Getter
        private State state;
        @Getter
        private final State initialState;

        private Collection<T> collection;
        private Iterator<T> iterator;
        private T instance;
        public enum State { INIT_INSTANCE, INSTANCE, INIT_COLLECTION, COLLECTION, TERMINAL }
        private final Function<P, T> getInstance;
        private final Function<P, Collection<T>> getCollection;

        private GetterState(final P parent, final State initialState, final Function<P, T> getInstance, final Function<P, Collection<T>> getCollection, final BiFunction<P, GetterState<P,T>, T> getter) {
            this.parent = parent;
            this.state = this.initialState = initialState;
            this.getInstance = getInstance;
            this.getCollection = getCollection;
            this.getter = getter;
        }
    }
}
