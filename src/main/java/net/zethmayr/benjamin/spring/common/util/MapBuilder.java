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

    /**
     * Creates a new builder populating
     * the given map.
     *
     * @param map A map
     * @param <K> The key type
     * @param <V> The value type
     * @param <M> The map type
     * @return A builder
     */
    public static <K, V, M extends Map<K, V>> MapBuilder<K, V, M> on(final M map) {
        return new MapBuilder<>(map);
    }

    /**
     * Creates a new builder populating
     * a {@link HashMap}.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A builder
     */
    public static <K, V> MapBuilder<K, V, HashMap<K, V>> hash() {
        return new MapBuilder<>(new HashMap<>());
    }

    /**
     * Creates a new builder populating
     * a {@link LinkedHashMap}.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A builder
     */
    public static <K, V> MapBuilder<K, V, LinkedHashMap<K, V>> linked() {
        return new MapBuilder<>(new LinkedHashMap<>());
    }

    /**
     * Creates a new builder populating
     * a {@link TreeMap}.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A builder
     */
    public static <K, V> MapBuilder<K, V, TreeMap<K, V>> tree() {
        return new MapBuilder<>(new TreeMap<>());
    }

    /**
     * Creates a new builder populating
     * a {@link Hashtable}.
     * Note that if you just want a synchronized map,
     * you can use the following:
     * <pre>
     * {@code
     * MapBuilder.<Long, Long>hash().in(Collections::synchronizedMap).put(134, 632634)
     * }
     * </pre>
     * This is provided primarily for compatibility with legacy code.
     *
     * @param <K> Ye key type
     * @param <V> Ye value type
     * @return Aye builder
     * @see java.util.Collections#synchronizedMap
     */
    public static <K, V> MapBuilder<K, V, Hashtable<K, V>> hashtable() {
        return new MapBuilder<>(new Hashtable<>());
    }

    /**
     * Puts an entry in the map.
     *
     * @param key   The key
     * @param value The value
     * @return the builder
     */
    public MapBuilder<K, V, M> put(final K key, final V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Returns a new builder populating
     * a map which is the result of
     * applying the given function
     * to the existing map. Usage:
     * <pre>
     * {@code
     * MapBuilder.<String, String>hash().put("a","a").in(Collections::unmodifiableMap)
     * }
     * </pre>
     * Note that this may change whether further mutations are supported.
     *
     * @param wrapper The wrapping or transforming function
     * @param <N>     The new map type
     * @return A new builder
     */
    public <N extends Map<K, V>> MapBuilder<K, V, N> in(final Function<M, N> wrapper) {
        return new MapBuilder<>(wrapper.apply(map));
    }

    @Override
    public M build() {
        return map;
    }
}
