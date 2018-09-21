package net.zethmayr.benjamin.spring.common.model.base;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class ModelTrustedTest {
    private static final class TestTrusting extends Trusting<TestTrusting> {}

    private static final class TestUntrusting {
        private boolean marshaling;

        public TestUntrusting marshaling(final boolean marshaling) {
            this.marshaling = marshaling;
            return this;
        }

        public boolean marshaling() {
            return marshaling;
        }
    }

    private static final class UnderTest extends ModelTrusted<UnderTest> {}

    private final ModelTrusted<UnderTest> underTest = new UnderTest();

    @Test
    public void setsForTrustingInstances() {
        final TestTrusting instance = new TestTrusting();

        assertThat(instance.marshaling(), is(false));

        assertThat(underTest.marshaling(instance), is(false));
        assertThat(underTest.marshaling(instance, true), is(underTest));
        assertThat(instance.marshaling(), is(true));
        assertThat(underTest.marshaling(instance), is(true));
        assertThat(underTest.marshaling(instance, false), is(underTest));
        assertThat(instance.marshaling(), is(false));
        assertThat(underTest.marshaling(instance), is(false));
    }

    @Test
    public void cannotGetTrustingInstance() {
        final TestTrusting instance = new TestTrusting();
        assertThat(instance, not(sameInstance(underTest)));

        assertThat(underTest.marshaling(), is(true));
        assertThat(underTest.marshaling(true), is(underTest));
        assertThat(underTest.marshaling(instance), is(false));
        assertThat(underTest.marshaling(instance, true), is(underTest));
        assertThat(underTest.marshaling(instance), is(true));
        assertThat(underTest.marshaling(instance, false), is(underTest));
        assertThat(underTest.marshaling(instance), is(false));
        assertThat(Trusting.class.cast(underTest), Matchers.<Trusting>allOf(
                is(sameInstance(underTest)),
                not(sameInstance(instance))
        ));
        assertThat(instance.marshaling(), is(false));
        assertThat(underTest.marshaling(), is(true));
    }

    @Test
    public void doesNothingForUntrustingInstances() {
        final TestUntrusting instance = new TestUntrusting();

        assertThat(instance.marshaling(), is(false));

        assertThat(underTest.marshaling(instance), is(false));
        assertThat(underTest.marshaling(instance, true), is(underTest));
        assertThat(instance.marshaling(), is(false));
        assertThat(underTest.marshaling(instance), is(false));
        assertThat(underTest.marshaling(instance, false), is(underTest));
        assertThat(instance.marshaling(), is(false));
        assertThat(underTest.marshaling(instance), is(false));

        instance.marshaling(true);
        assertThat(instance.marshaling(), is(true));
    }

}
