package net.zethmayr.benjamin.spring.common.util;

import lombok.val;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
                .add(new Date(), // time
                        new Date(), // is marching on
                        new Date())
                .add(new Date(), // and time
                        new Date())
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
    public void canBuildAnArrayListThenTrimIt() {
        final List<Integer> built = ListBuilder.array(1, 2, 3)
                .then(l -> l.ensureCapacity(10))
                .add(4, 5, 6)
                .then(ArrayList::trimToSize)
                .in(Collections::unmodifiableList)
                .build();
        assertThat(built, isA(List.class));
        assertThat(built, contains(1, 2, 3, 4, 5, 6));
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
    public void canMutateEachElementOfALinkedList() {
        val populationCounter = new AtomicInteger(0);
        val start = System.nanoTime();
        final Supplier<Long> populator = () -> {
            if (populationCounter.incrementAndGet() < 1000) {
                return System.nanoTime() - start;
            } else {
                return null;
            }
        };
        final Consumer<Long> printer = System.out::println;
        val built = ListBuilder.<Long>linked()
                .add(populator)
                .forEach(printer)
                .toEach(i -> (System.nanoTime() - start) - i)
                .build();
        built.forEach(printer);
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
