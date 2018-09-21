package net.zethmayr.benjamin.spring.common.mapper.base;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class PsSetterFactoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void aRoseByAnyOtherName() throws Exception {
        assertThat(PsSetterFactory::shortString, isA(PsSetterFactory.class));
        assertThat(PsSetterFactory::integer, isA(PsSetterFactory.class));
        assertThat(PsSetterFactory::longInteger, isA(PsSetterFactory.class));
    }

    @Test
    public void stringInstancesWrapExceptions() throws Exception {
        thrown.expect(MappingException.class);
        final PsSetter<String> underTest = PsSetterFactory.shortString(1);
        final PreparedStatement evil = mock(PreparedStatement.class);
        doThrow(new SQLException())
                .when(evil).setString(eq(1),eq("test"));
        underTest.apply(evil, "test");
    }

    @Test
    public void integerInstancesWrapExceptions() throws Exception {
        thrown.expect(MappingException.class);
        final PsSetter<Integer> underTest = PsSetterFactory.integer(2);
        final PreparedStatement evil = mock(PreparedStatement.class);
        doThrow(new SQLException())
                .when(evil).setInt(eq(2),eq(3));
        underTest.apply(evil, 3);
    }

    @Test
    public void longInstancesWrapExceptions() throws Exception {
        thrown.expect(MappingException.class);
        final PsSetter<Long> underTest = PsSetterFactory.longInteger(4);
        final PreparedStatement evil = mock(PreparedStatement.class);
        doThrow(new SQLException())
                .when(evil).setLong(eq(4),eq(5L));
        underTest.apply(evil,5L);
    }
}
