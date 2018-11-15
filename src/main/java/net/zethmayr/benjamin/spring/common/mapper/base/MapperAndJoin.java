package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static lombok.AccessLevel.PUBLIC;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.COLLECTION;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INIT_COLLECTION;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INIT_INSTANCE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.INSTANCE;

@Builder
@Accessors(fluent = true)
public class MapperAndJoin<P, T> {
    @Getter(PUBLIC)
    @NonNull
    private final InvertibleRowMapper<T> mapper;
    @Getter(PUBLIC)
    @NonNull
    private final BiConsumer<P, T> parentAcceptor;
    @Getter(PUBLIC)
    @NonNull
    private final Function<P,GetterState<P,T>> getterStateFactory;
    @Getter(PUBLIC)
    @NonNull
    private final BiFunction<P, GetterState<P,T>, T> parentGetter;
    @Getter(PUBLIC)
    @NonNull
    private final Mapper<P, ?, ?> parentField;
    @Getter(PUBLIC)
    @NonNull
    private final SqlOp relation;
    @Getter(PUBLIC)
    @NonNull
    private final Mapper<T, ?, ?> relatedField;

    public static <T, P> Function<P,GetterState<P,T>> instanceState(final Function<P, T> getInstance) {
        return (parent) -> new GetterState<>(parent, INIT_INSTANCE, getInstance, null);
    }

    public static <T, P> Function<P,GetterState<P,T>> collectionState(final Function<P, Collection<T>> getCollection) {
        return (parent) -> new GetterState<>(parent, INIT_COLLECTION, null, getCollection);
    }

    public Object[] getInsertValues(final P parent) {
        val tempGetterState = getterStateFactory.apply(parent);
        /*
         * Trouble Ahead
         *
         * This method is senseless for collections, and has trouble with optional fields.
         */
        return mapper.getInsertValues(parentGetter.apply(parent, tempGetterState));
    }

    public static <T, P> BiFunction<P, GetterState<P,T>, T> instanceGetter() {
        return (parent, state) -> {
            if (state.state == INIT_INSTANCE) {
                state.instance = state.getInstance.apply(parent);
                state.state = INSTANCE;
            }
            return state.instance;
        };
    }

    public static <T, P> BiFunction<P, GetterState<P,T>, T> collectionGetter() {
        return (parent, state) -> {
            if (state.state == INIT_COLLECTION) {
                state.collection = state.getCollection.apply(parent);
                state.iterator = state.collection.iterator();
                state.state = COLLECTION;
            }
            if (state.iterator.hasNext()) {
                return state.iterator.next();
            } else {
                return null;
            }
        };
    }

    @Accessors(fluent = true)
    public static class GetterState<P, T> {
        @Getter
        private final P parent;

        private Collection<T> collection;
        private Iterator<T> iterator;
        private T instance;
        public enum State { INIT_INSTANCE, INSTANCE, INIT_COLLECTION, COLLECTION }
        @Getter
        private State state;
        @Getter
        private final State initialState;
        private final Function<P, T> getInstance;
        private final Function<P, Collection<T>> getCollection;

        private GetterState(final P parent, final State initialState, final Function<P, T> getInstance, final Function<P, Collection<T>> getCollection) {
            this.parent = parent;
            this.state = this.initialState = initialState;
            this.getInstance = getInstance;
            this.getCollection = getCollection;
        }
    }
}
