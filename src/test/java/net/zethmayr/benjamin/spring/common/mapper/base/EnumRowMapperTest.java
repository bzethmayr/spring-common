package net.zethmayr.benjamin.spring.common.mapper.base;

import net.zethmayr.benjamin.spring.common.mapper.TestEnumMapper;
import net.zethmayr.benjamin.spring.common.model.TestEnum;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.function.Supplier;

import static net.zethmayr.benjamin.spring.common.model.TestEnum.MAYBE;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnumRowMapperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void detectsBadSetupWhenQueriesNotCreated() {
        thrown.expect(MappingException.class);
        thrown.expectMessage(MappingException.BAD_SETUP);

        class TestMapper extends EnumRowMapper<TestEnum> {

            private TestMapper() {
                super(TestEnum.YOU_HAVE_OFFENDED_ME, Collections.emptyList(), null, null, null);
            }
        }
        new TestMapper();
    }

    @Test
    public void cannotGetEmptyInstance() {
        thrown.expect(UnsupportedOperationException.class);
        final TestEnumMapper underTest = new TestEnumMapper();
        final Supplier<TestEnum> empty = underTest.empty();
        assertThat(empty, not(nullValue()));
        empty.get();
    }

    @Test
    public void canGetInsertValues() {
        final TestEnumMapper underTest = new TestEnumMapper();
        assertThat(underTest, isA(EnumRowMapper.class));
        final Object[] toInsert = underTest.getInsertValues(MAYBE);
        assertThat(toInsert, arrayWithSize(3));
        assertThat(toInsert, arrayContaining(2,"maybe","true"));
    }

    @Test
    public void canMapFromRs() throws Exception {
        final TestEnumMapper underTest = new TestEnumMapper();
        assertThat(underTest.idMapper(), isA(ClassFieldMapper.class));
        final ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getInt("id")).thenReturn(2);
        final TestEnum read = underTest.mapRow(mockRs, 0);
        assertThat(read, is(MAYBE));
    }
}
