package net.zethmayr.benjamin.spring.common.mapper.base;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RsGetterFactoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void aRoseByAnyOtherName() {
        assertThat(RsGetterFactory::string, isA(RsGetterFactory.class));
        assertThat(RsGetterFactory::integer, isA(RsGetterFactory.class));
        assertThat(RsGetterFactory.string(), isA(RsGetterFactory.class));
        assertThat(RsGetterFactory.integer(), isA(RsGetterFactory.class));
    }

    @Test
    public void stringInstancesWrapExceptions() throws Exception {
        thrown.expect(MappingException.class);
        final RsGetter<String> underTest = RsGetterFactory.string("test");
        final ResultSet evil = mock(ResultSet.class);
        when(evil.getString("test")).thenThrow(new SQLException());
        underTest.from(evil);
    }

    @Test
    public void integerInstancesWrapExceptions() throws Exception {
        thrown.expect(MappingException.class);
        final RsGetter<Integer> underTest = RsGetterFactory.integer("test");
        final ResultSet evil = mock(ResultSet.class);
        when(evil.getInt("test")).thenThrow(new SQLException());
        underTest.from(evil);
    }

    @Test
    public void longInstancesWrapExceptions() throws Exception {
        thrown.expect(MappingException.class);
        final RsGetter<Long> underTest = RsGetterFactory.longInteger("test");
        final ResultSet evil = mock(ResultSet.class);
        when(evil.getLong("test")).thenThrow(new SQLException());
        underTest.from(evil);
    }
}
