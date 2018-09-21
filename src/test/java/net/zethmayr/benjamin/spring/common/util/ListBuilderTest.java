package net.zethmayr.benjamin.spring.common.util;

import lombok.val;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class ListBuilderTest {
    @Test
    public void canBuildOnProvidedList() {
        val original = new ArrayList<Date>();
        val built = ListBuilder.on(original)
                .add(new Date()) // time
                .add(new Date()) // is marching on
                .add(new Date())
                .add(new Date()) // and time
                .add(new Date())
                .add(new Date()) // is still marching on
                .build();
        assertThat(built, sameInstance(original));
        assertThat(built, hasSize(6));
    }

    @Test
    public void canBuildAnArrayList() {
        val built = ListBuilder.<String>array()
                .add("a").add("b").add("c")
                .build();
        assertThat(built, isA(ArrayList.class));
        assertThat(built, contains("a", "b", "c"));
    }

    @Test
    public void canBuildALinkedList() {
        val built = ListBuilder.<Integer>linked()
                .add(1).add(4).add(2).add(1)
                .build();
        assertThat(built, isA(LinkedList.class));
        assertThat(built, hasSize(4));
    }

    @Test
    public void canBuildAVector() {
        val built = ListBuilder.<Integer>vector()
                .add(2).add(3).add(5).add(Integer.MAX_VALUE)
                .build();
        assertThat(built, isA(Vector.class));
        assertThat(built, hasSize(4));
    }

    @Test
    public void canBuildASynchronizedArrayList() {
        val built = ListBuilder.<Integer>array()
                .in(Collections::synchronizedList)
                .add(1).add(1).add(1).add(1).add(2)
                .build();
        assertThat(built, hasSize(5));
    }
}
