package net.zethmayr.benjamin.spring.common.repository.base;

import net.zethmayr.benjamin.spring.common.model.TestEnum;
import net.zethmayr.benjamin.spring.common.repository.TestEnumRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EnumMapperRepositoryTest {

    @Autowired
    private TestEnumRepository underTest;

    @Autowired
    private AbstractSchemaService schemaService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        schemaService.applySchemaFor(underTest);
    }

    @After
    public void tearDown() {
        schemaService.nuke(underTest);
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

    @Test
    public void canWriteAllThenRead() {
        schemaService.writeDataFor(underTest, TestEnum.values());
        final List<TestEnum> all = underTest.getAll();
        for (int i = 0, n = TestEnum.values().length; i < n; i++) {
            assertThat(all.get(i), is(TestEnum.values()[i]));
        }
    }

    @Test
    public void cannotDelete() {
        thrown.expect(UnsupportedOperationException.class);
        underTest.delete(0);
    }

    @Test
    public void cannotDeleteMonadic() {
        thrown.expect(UnsupportedOperationException.class);
        underTest.deleteMonadic(null);
    }
}
