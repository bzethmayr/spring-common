package net.zethmayr.benjamin.spring.common.mapper;

import lombok.val;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

// golly, what an awful name
public class TestUserOrdersMapperTest {
    private TestUserOrdersMapper underTest;

    @Before
    public void setUp() {
        underTest = new TestUserOrdersMapper();
    }

    @Test
    public void canInstantiateIndependent() {
        val underTest = new TestUserOrdersMapper();
        assertThat(underTest, isA(TestUserOrdersMapper.class));
    }
}
