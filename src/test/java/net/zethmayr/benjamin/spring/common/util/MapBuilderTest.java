package net.zethmayr.benjamin.spring.common.util;

import lombok.val;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

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
}
