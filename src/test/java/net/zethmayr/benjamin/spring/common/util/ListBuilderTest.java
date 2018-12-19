package net.zethmayr.benjamin.spring.common.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@Slf4j
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
                .add("a").add("b", "c")
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
    public void canAddAListToAListToAList() {
        val built = ListBuilder.array(1).add(ListBuilder.linked(2).add(ListBuilder.vector(3).build()).build()).build();
        assertThat(built, isA(ArrayList.class));
        assertThat(built, contains(1, 2, 3));
    }

    @Test
    public void canVisitList() {
        val builder = ListBuilder.<Integer>array().add(ListBuilder.generator((i) -> i, 10));
        val last = new AtomicInteger(-1);
        builder.forEach((i) -> assertThat(i, is(last.getAndSet(i) + 1)));
    }

    @Test
    public void toEachCanAddIndex() {
        final UnaryOperator<Integer> plusOne = i -> i + 1;
        final Supplier<Supplier<Integer>> populatorFactory = () ->
                ListBuilder.generator(plusOne, 10);
        val built = ListBuilder.<Integer>array().add(populatorFactory.get());
        assertThat(built.build(), contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        val addPopulator = populatorFactory.get();
        final UnaryOperator<Integer> adder = (e) -> e + addPopulator.get();
        built.toEach(adder);
        assertThat(built.build(), contains(2, 4, 6, 8, 10, 12, 14, 16, 18, 20));
    }

    @Test
    public void canGenerateSquaresTo100() {
        final UnaryOperator<Integer> squareTil100 = i -> {
            i = i + 1;
            val square = i * i;
            return square <= 100 ? square : null;
        };
        val built = ListBuilder.<Integer>linked().add(ListBuilder.generator(squareTil100)).build();
        assertThat(built, hasSize(10));
        assertThat(built.getFirst(), is(1));
        assertThat(built.getLast(), is(100));
    }

    @Test
    public void canPopulateFromIterator() {
        val first = ListBuilder.<Integer>array().add(ListBuilder.generator((i) -> i, 100)).build();
        val built = ListBuilder.<Integer>linked().add(ListBuilder.generator(first.iterator())).build();
        assertThat(built.getFirst(), is(0));
        assertThat(built.getLast(), is(99));
    }

    @Test
    public void canUseWeirdStatefulGeneratorsIndependentOfTheIndex() {
        val enclosed = new int[2];
        final Supplier<Integer> until100 = () -> {
            // the first term of the fibonacci sequence is defined as 1
            if (enclosed[0] == 0) {
                enclosed[0] = 1;
                return enclosed[0];
            }
            // the second term of the fibonacci sequence is also defined as 1
            if (enclosed[1] == 0) {
                enclosed[1] = 1;
                return enclosed[1];
            }
            // each subsequent term is the sum of the prior two terms
            val sum = enclosed[0] + enclosed[1];
            enclosed[0] = enclosed[1];
            enclosed[1] = sum;
            if (sum <= 100) {
                return sum;
            }
            return null;
        };
        val built = ListBuilder.<Integer>linked().add(until100).build();
        assertThat(built, hasSize(11));
        assertThat(built.getLast(), is(89));
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
