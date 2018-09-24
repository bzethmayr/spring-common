package net.zethmayr.benjamin.spring.common.repository.base;

import net.zethmayr.benjamin.spring.common.model.TestEnum;
import net.zethmayr.benjamin.spring.common.repository.TestSchemaService;
import net.zethmayr.benjamin.spring.common.repository.TestSingleWiredEnumRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SingleWiredEnumMapperRepositoryTest {

    @Autowired
    private TestSingleWiredEnumRepository underTest;

    @Autowired
    private TestSchemaService schema;

    @Before
    public void setUp() {
        schema.applySchemaFor(underTest);
    }

    @After
    public void cleanUp() {
        schema.nuke(underTest);
    }

    @Test
    public void canWriteThenRead() {
        for (final TestEnum each : TestEnum.values()) {
            underTest.insert(each);
        }
        final List<TestEnum> all = underTest.getAll();
        for (int i = 0, n = TestEnum.values().length; i < n; i++) {
            assertThat(all.get(i), is(TestEnum.values()[i]));
        }
    }
}
