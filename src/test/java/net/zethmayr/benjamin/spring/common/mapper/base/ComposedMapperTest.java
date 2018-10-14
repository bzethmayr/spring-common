package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.val;
import net.zethmayr.benjamin.spring.common.model.History;
import net.zethmayr.benjamin.spring.common.model.Holder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ComposedMapperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ComposedMapper<Holder<Integer>, Integer, String> underTest;

    @Before
    public void setUp() {
        underTest = new ComposedMapper<>(
                "test",
                Holder::get,
                String::valueOf,
                ColumnType.SHORT_STRING,
                Integer::valueOf,
                Holder::set
        );
    }

    @Test
    public void typesCorrespond() {
        final Class<?> external = underTest.getExternalClass();
        final Holder<Integer> pojo = new Holder<>(23);
        assertThat(external, sameInstance(String.class));
        assertThat(underTest.serFrom(pojo), instanceOf(external));
    }

    @Test
    public void hasSqlType() {
        final String sqlType = underTest.sqlType();
        assertThat(sqlType, not(isEmptyOrNullString()));
        assertThat(sqlType, is(underTest.getColumnType().sqlType()));
    }

    @Test
    public void psApplyThrowsWhenOrdinalNotSet() {
        thrown.expect(MappingException.class);
        thrown.expectMessage(MappingException.BAD_SETUP);
        assertThat(underTest.getInsertOrdinal(), is(-1));
        underTest.apply(mock(PreparedStatement.class), "test");
    }

    @Test
    public void canApplyToPs() throws Exception {
        final Holder<Integer> container = new Holder<>(23);
        underTest.setInsertOrdinal(1);
        final PreparedStatement mockPs = mock(PreparedStatement.class);
        underTest.apply(mockPs, underTest.serFrom(container));
        verify(mockPs).setString(1, "23");
        verifyNoMoreInteractions(mockPs);
    }

    @Test
    public void canApplyNullToPs() throws Exception {
        final Holder<Integer> container = new Holder<>(null);
        underTest.setInsertOrdinal(1);
        final PreparedStatement mockPs = mock(PreparedStatement.class);
        underTest.apply(mockPs, underTest.serFrom(container));
        verify(mockPs).setNull(1, Types.CHAR);
        verifyNoMoreInteractions(mockPs);
    }

    @Test
    public void cannotSetConflictingOrdinal() {
        thrown.expect(MappingException.class);
        thrown.expectMessage(MappingException.BAD_SETUP);
        underTest.setInsertOrdinal(1);
        underTest.setInsertOrdinal(2);
    }

    @Test
    public void canSetConsistentOrdinalThoughIDoNotKnowWhyThisHappensDotDotDot() {
        underTest.setInsertOrdinal(1);
        underTest.setInsertOrdinal(1);
    }

    @Test
    public void canGetFieldName() {
        assertThat(underTest.fieldName(), is("test"));
    }

    @Test
    public void canSetFromRs() throws Exception {
        final Holder<Integer> acceptor = new Holder<>(0);
        final ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getString("test")).thenReturn("7");
        underTest.desTo(acceptor, mockRs);
        assertThat(acceptor.get(), is(7));
    }

    @Test
    public void canSetFromSqlValue() throws Exception {
        final Holder<Integer> acceptor = new Holder<>(0);
        underTest.desTo(acceptor, "23");
        assertThat(acceptor.get(), is(23));
    }

    @Test
    public void passesNullsFromRs() throws Exception {
        final Holder<Integer> acceptor = new Holder<>(0);
        final ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getString("test")).thenReturn(null);
        underTest.desTo(acceptor, mockRs);
        assertThat(acceptor.get(), is(nullValue()));
    }

    @Test
    public void throwsWhenRsThrows() throws Exception {
        thrown.expect(MappingException.class);
        final Holder<Integer> acceptor = new Holder<>(0);
        final ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getString("test")).thenThrow(new SQLException());
        underTest.desTo(acceptor, mockRs);
    }

    @Test
    public void throwsWhenRsWasNullThrows() throws Exception {
        thrown.expect(MappingException.class);
        final Holder<Integer> acceptor = new Holder<>(0);
        final ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getString("test")).thenReturn("5");
        when(mockRs.wasNull()).thenThrow(new SQLException());
        underTest.desTo(acceptor, mockRs);
    }

    @Test
    public void rethrowsMappingExceptionUnwrapped() throws Exception {
        val expected = MappingException.badSetup("four cups");
        thrown.expect(sameInstance(expected));
        final Holder<Integer> acceptor = new Holder<>(0);
        final ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getString("test")).thenReturn("5");
        when(mockRs.wasNull()).thenThrow(expected);
        underTest.desTo(acceptor, mockRs);
    }

    @Test
    public void canGetForRow() throws Exception {
        final Holder<Integer> container = new Holder<>(7);
        final String forRow = underTest.serFrom(container);
        assertThat(forRow, is("7"));
    }

    @Test
    public void canGetNullForRow() throws Exception {
        final Holder<Integer> container = new Holder<>(null);
        final String forRow = underTest.serFrom(container);
        assertThat(forRow, is(nullValue()));
    }


    @Test
    public void canGetFieldMapper() {
        final Mapper<Holder<Integer>, Integer, String> underTest = ComposedMapper.field(
                "test",
                Holder::get,
                String::valueOf,
                ColumnType.SHORT_STRING,
                Integer::valueOf,
                Holder::set
        );
        assertThat(underTest, isA(Mapper.class));
        assertThat(underTest.ser(7), is("7"));
        assertThat(underTest.des(underTest.ser(7)), is(7));
        assertThat(underTest.des(underTest.ser(null)), is(nullValue()));
    }

    @Test
    public void canGetDirectMapper() {
        final Mapper<Holder<String>, String, String> underTest = ComposedMapper.simpleField(
                "test",
                Holder::get,
                ColumnType.SHORT_STRING,
                Holder::set
        );
        assertThat(underTest, isA(Mapper.class));
        assertThat(underTest.ser("7"), is("7"));
        assertThat(underTest.des("7"), is(underTest.ser("7")));
        assertThat(underTest.des(underTest.ser(null)), is(nullValue()));
    }

    @Test
    public void canGetMoneyMapper() {
        final Mapper<Holder<BigDecimal>, BigDecimal, Long> underTest = ComposedMapper.money(
            "test",
            Holder::get,
            Holder::set
        );
        assertThat(underTest, isA(Mapper.class));
        assertThat(underTest.ser(new BigDecimal("1.00")), is(100L));
        assertThat(underTest.des(100L), is(new BigDecimal("1.00")));
        assertThat(underTest.des(underTest.ser(null)), is(nullValue()));
    }

    @Test
    public void canGetEnumFieldMapper() {
        final Mapper<History, Instant, Long> underTest = ComposedMapper.enumField(
                "event_instant_estimated",
                History::when,
                Instant::toEpochMilli,
                ColumnType.LONG,
                Instant::ofEpochMilli
        );
        assertThat(underTest.getFrom(History.MAGNA_CARTA), is(History.MAGNA_CARTA.when()));
        final long millis = underTest.serFrom(History.MAGNA_CARTA);
        System.out.println(millis);
        assertThat(underTest.des(millis), is(History.MAGNA_CARTA.when()));
    }

    @Test
    public void canGetEnumDirectMapper() throws Exception {
        final Mapper<History, String, String> underTest = ComposedMapper.enumSimple(
                "event_year",
                History::year,
                ColumnType.SHORT_STRING
        );
        assertThat(underTest.getFrom(History.COLUMBUS), is("1492"));
        assertThat(underTest.serFrom(History.COLUMBUS), is("1492"));
        assertThat(underTest.des("1492"), is(History.COLUMBUS.year()));
    }

    private Mapper<History, History, Integer> getEnumFieldWhichIsEnumMapper() {
        return ComposedMapper.field(
                "prior_related_event",
                History::getPriorRelated,
                History::ordinal,
                ColumnType.INTEGER,
                (i) -> History.values()[i],
                ComposedMapper.cantSetTo()
        );
    }

    @Test
    public void canGetEnumFieldWhichIsEnumMapper() {
        final Mapper<History, History, Integer> underTest = getEnumFieldWhichIsEnumMapper();
        assertThat(underTest.getFrom(History.MAGNA_CARTA), is(nullValue()));
        assertThat(underTest.serFrom(History.MAGNA_CARTA), is(nullValue()));
        assertThat(underTest.ser(History.MAGNA_CARTA), is(0));
        assertThat(underTest.ser(History.DECLARATION_OF_INDEPENDENCE), is(2));
        assertThat(underTest.getFrom(History.DECLARATION_OF_INDEPENDENCE), is(History.MAGNA_CARTA));
        assertThat(underTest.serFrom(History.DECLARATION_OF_INDEPENDENCE), is(0));
        assertThat(underTest.des(2), is(History.DECLARATION_OF_INDEPENDENCE));
        assertThat(underTest.des(underTest.serFrom(History.DECLARATION_OF_INDEPENDENCE)), is(History.MAGNA_CARTA));
    }

    @Test
    public void cantSetToEnumFieldsFromSerValues() {
        thrown.expect(UnsupportedOperationException.class);
        final Mapper<History, History, Integer> underTest = getEnumFieldWhichIsEnumMapper();
        final History toNotBeAbleToDesTo = History.COLUMBUS;
        underTest.desTo(toNotBeAbleToDesTo, 1);
    }

    @Test
    public void cantSetToEnumFieldsFromRs() throws Exception {
        thrown.expect(UnsupportedOperationException.class);
        final Mapper<History, History, Integer> underTest = getEnumFieldWhichIsEnumMapper();
        final History toNotBeAbleToDesTo = History.COLUMBUS;
        final ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getInt("id")).thenReturn(1);
        underTest.desTo(toNotBeAbleToDesTo, mockRs);
    }
}
