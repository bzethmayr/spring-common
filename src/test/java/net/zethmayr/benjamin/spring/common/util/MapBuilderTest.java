package net.zethmayr.benjamin.spring.common.util;

import lombok.val;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class MapBuilderTest {

    @Test
    public void canBuildOnProvidedMap() {
        val original = new HashMap<String, Integer>();
        val populated = MapBuilder.on(original)
                .put("once", 1)
                .put("the loneliest number", 1)
                .put("the answer", 42)
                .put(" appa sdg", -1)
                .build();
        assertThat(populated, sameInstance(original));
        assertThat(populated.size(), is(4));
    }

    @Test
    public void canWrapInUnmodifiable() {
        val wrapped = MapBuilder.<String, Integer>tree()
                .put("apple", 900)
                .put("banana", 990)
                .put("broccoli", 90)
                .put("moldy broccoli", 99)
                .in(Collections::unmodifiableNavigableMap)
                .build();
        Exception expected = null;
        try {
            wrapped.put("lemon", 990);
        } catch (Exception e) {
            expected = e;
        }
        assertThat(expected, instanceOf(UnsupportedOperationException.class));
        assertThat(wrapped.size(), is(4));
    }

    @Test
    public void canBuildOnProvidedAndWrap() {
        val original = new TreeMap<String,String>();
        val built = MapBuilder.on(original)
                .put("unmp","tsiss")
                .put("banana","split")
                .put("stock","value")
                .in(Collections::unmodifiableNavigableMap)
                .build();
        assertThat(built, not(sameInstance(original)));
        assertThat(built.floorKey("baseball"), is("banana"));
        Exception expected = null;
        try {
            built.put("squeadily","meatily");
        } catch (Exception e) {
            expected = e;
        }
        assertThat(expected, instanceOf(UnsupportedOperationException.class));
        assertThat(built.size(), is(3));
    }

    @Test
    public void canBuildAHashMap() {
        val built = MapBuilder.<Integer, String>hash()
                .put(1, "singular sensation")
                .put(2, "many dancing people")
                .put(3, "sheets")
                .put(4, "sleeping")
                .build();
        assertThat(built, isA(HashMap.class));
        assertThat(built.size(), is(4));
    }

    @Test
    public void canBuildALinkedHashMap() {
        val built = MapBuilder.<Integer, String>linked()
                .put(1, "singular sensation")
                .put(2, "many dancing people")
                .put(3, "sheets")
                .put(4, "sleeping")
                .build();
        assertThat(built, isA(LinkedHashMap.class));
        assertThat(built.size(), is(4));
        assertThat(built.values().iterator().next(), is("singular sensation"));
    }

    @Test
    public void canBuildATreeMap() {
        val built = MapBuilder.<Integer, String>tree()
                .put(4, "sleeping")
                .put(3, "sheets")
                .put(2, "many dancing people")
                .put(1, "singular sensation")
                .build();
        assertThat(built, isA(TreeMap.class));
        assertThat(built.size(), is(4));
        assertThat(built.values().iterator().next(), is("singular sensation"));
    }

    @Test
    public void canBuildAHashtable() {
        val built = MapBuilder.<Integer, String>hashtable()
                .put(1, "plow to pull with oxen")
                .put(2, "hands to rake in filth with")
                .put(3, "times slower than needed")
                .build();
        assertThat(built, isA(Hashtable.class));
        assertThat(built.size(), is(3));
    }

    private class NopKeyValueConsumer implements BiConsumer<String, String> {
        @Override
        public void accept(final String key, final String value) {

        }
    }

    @Test
    public void canVisitMapInProgress() {
        val consumer = spy(new NopKeyValueConsumer());
        val builder = MapBuilder.<String, String>hash()
                .put("a", "a").put("b", "d").put("c", "z")
                .forEach(consumer);
        verify(consumer).accept("a", "a");
        verify(consumer).accept("b", "d");
        verify(consumer).accept("c", "z");
    }

    @Test
    public void canInvertMapInProgress() {
        BiFunction<String,String,String> keyToValue = (k, v) -> v;
        BiFunction<String,String,String> valueToKey = (k, v) -> k;
        val built = MapBuilder.<String,String>linked()
                .put("1", "a").put("2", "b").put("3", "c").put("4", "d")
                .toEach(keyToValue, valueToKey)
                .build();
        assertThat(built.keySet(), contains("a", "b", "c", "d"));
        assertThat(built.values(), contains("1", "2", "3", "4"));
    }


    @Test
    public void canRewriteKeysInProgress() {
        final BiFunction<Integer, Integer, Integer> keyValueAdder = (k, v) -> k + v;
        val built = MapBuilder.on(new LinkedHashMap<Integer, Integer>())
                .put(1, 10).put(2, 20).put(3, 30)
                .toEachKey(keyValueAdder)
                .build();
        assertThat(built.keySet(), contains(11, 22, 33));
        assertThat(built.get(11), is(10));
    }

    @Test
    public void canRewriteValuesInProgress() {
        final BiFunction<Integer, String, String> valueKeyAppender = (k, v) -> v + k;
        val built = MapBuilder.<Integer, String>linked()
                .put(1, "a").put(2, "b").put(3, "a")
                .toEachValue(valueKeyAppender)
                .build();
        assertThat(built.values(), contains("a1", "b2", "a3"));
    }
}
