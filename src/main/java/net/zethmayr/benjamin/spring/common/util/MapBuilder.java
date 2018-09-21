package net.zethmayr.benjamin.spring.common.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class MapBuilder<K, V, M extends Map<K, V>> implements Builder<M> {

    private final M map;

    private MapBuilder(final M map) {
        this.map = map;
    }

    public static <K, V, M extends Map<K, V>> MapBuilder<K, V, M> on(final M map) {
        return new MapBuilder<>(map);
    }

    public static <K, V> MapBuilder<K, V, Hashtable<K, V>> hashtable() {
        return new MapBuilder<>(new Hashtable<>());
    }

    public static <K, V> MapBuilder<K, V, HashMap<K, V>> hash() {
        return new MapBuilder<>(new HashMap<>());
    }

    public static <K, V> MapBuilder<K, V, LinkedHashMap<K, V>> linked() {
        return new MapBuilder<>(new LinkedHashMap<>());
    }

    public static <K, V> MapBuilder<K, V, TreeMap<K, V>> tree() {
        return new MapBuilder<>(new TreeMap<>());
    }

    public MapBuilder<K, V, M> put(final K key, final V value) {
        map.put(key, value);
        return this;
    }

    public <N extends Map<K, V>> MapBuilder<K, V, N> in(final Function<M, N> wrapper) {
        return new MapBuilder<>(wrapper.apply(map));
    }

    @Override
    public M build() {
        return map;
    }
}
