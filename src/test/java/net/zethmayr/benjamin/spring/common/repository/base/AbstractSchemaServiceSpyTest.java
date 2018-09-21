package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.val;
import net.zethmayr.benjamin.spring.common.model.TestEnum;
import net.zethmayr.benjamin.spring.common.repository.TestEnumRepository;
import net.zethmayr.benjamin.spring.common.repository.TestSchemaService;
import net.zethmayr.benjamin.spring.common.service.Breaker;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class AbstractSchemaServiceSpyTest {

    @Autowired
    private TestSchemaService underTest;

    @SpyBean
    private JdbcTemplate db;

    @SpyBean
    private TestEnumRepository enumRepository;

    @SpyBean
    private Breaker breaker;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @After
    public void tearDown() {
        underTest.nuke(enumRepository);
    }

    @Test
    public void writeDataRollsBackOnMapperError() {
        underTest.applySchemaFor(enumRepository);
        val values = enumRepository.mapper().enumValues();
        final TestEnum first = values[0];
        final TestEnum last = values[values.length - 1];
        enumRepository.insert(last);

        assertThat(enumRepository.getMonadic(first).isPresent(), is(false));
        assertThat(enumRepository.getMonadic(last).isPresent(), is(true));

        doThrow(new RuntimeException()).when(breaker).breaker(1);
        underTest.maybeFailToWriteDataFor(enumRepository, new ArrayList<>(), values);

        assertThat(enumRepository.getMonadic(first).isPresent(), is(false));
        assertThat(enumRepository.getMonadic(last).isPresent(), is(true));
    }

    @Test
    public void writeDataRollsBackOnPreInsertError() {
        underTest.applySchemaFor(enumRepository);
        val values = enumRepository.mapper().enumValues();
        final TestEnum first = values[0];
        final TestEnum last = values[values.length - 1];
        enumRepository.insert(last);

        assertThat(enumRepository.getMonadic(first).isPresent(), is(false));
        assertThat(enumRepository.getMonadic(last).isPresent(), is(true));

        doThrow(new RuntimeException()).when(breaker).breaker(1, 9);
        underTest.maybeFailToWriteDataFor(enumRepository, new ArrayList<>(), values);

        assertThat(enumRepository.getMonadic(first).isPresent(), is(false));
        assertThat(enumRepository.getMonadic(last).isPresent(), is(true));
    }

    @Test
    public void writeDataRollsBackOnPostInsertError() {
        underTest.applySchemaFor(enumRepository);
        val values = enumRepository.mapper().enumValues();
        final TestEnum first = values[0];
        final TestEnum last = values[values.length - 1];
        enumRepository.insert(last);

        assertThat(enumRepository.getMonadic(first).isPresent(), is(false));
        assertThat(enumRepository.getMonadic(last).isPresent(), is(true));

        doThrow(new RuntimeException()).when(breaker).breaker(2);
        underTest.maybeFailToWriteDataFor(enumRepository, new ArrayList<>(), values);

        assertThat(enumRepository.getMonadic(first).isPresent(), is(false));
        assertThat(enumRepository.getMonadic(last).isPresent(), is(true));
    }
}
