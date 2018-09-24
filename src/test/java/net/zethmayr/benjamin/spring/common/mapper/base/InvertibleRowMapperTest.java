package net.zethmayr.benjamin.spring.common.mapper.base;

import net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;

import static net.zethmayr.benjamin.spring.common.model.History.MAGNA_CARTA;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InvertibleRowMapperTest {
    private InvertibleRowMapper<TestPojo> underTest;

    @Before
    public void setUp() {
        underTest = new TestPojoMapper();
    }

    @Test
    public void exists() {
        assertThat(underTest, isA(RowMapper.class));
        assertThat(underTest.table(), is(TestPojoMapper.TABLE));
        assertThat(underTest.fields(), is(TestPojoMapper.FIELDS));
        assertThat(underTest.rowClass(), sameInstance(TestPojo.class));
        assertThat(underTest.insert(), not(isEmptyOrNullString()));
        assertThat(underTest.select(), not(isEmptyOrNullString()));
    }

    private static final String COMMENT = "expected";
    private static final BigDecimal WEIGHT = new BigDecimal("23.01");

    @Test
    public void canGenerateInsertValues() {
        final TestPojo pojoToInsert = new TestPojo()
                .setComment(COMMENT)
                .setEvent(MAGNA_CARTA)
                .setWeighting(WEIGHT);
        final Object[] toInsert = underTest.getInsertValues(pojoToInsert);
        assertThat(toInsert, arrayContaining(0, COMMENT, null, "23.01"));
    }

    @Test
    public void canMapFromRs() throws Exception {
        final ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getInt(TestPojoMapper.ID.fieldName)).thenReturn(73);
        when(mockRs.getString(TestPojoMapper.WEIGHTING.fieldName)).thenReturn("23.01");
        when(mockRs.getString(TestPojoMapper.COMMENT.fieldName)).thenReturn(COMMENT);
        when(mockRs.getInt(TestPojoMapper.EVENT.fieldName)).thenReturn(MAGNA_CARTA.ordinal());
        final TestPojo read = underTest.mapRow(mockRs, 0);
        assertThat(read.getComment(), is(COMMENT));
        assertThat(read.getWeighting(), is(WEIGHT));
        assertThat(read.getEvent(), is(MAGNA_CARTA));
        assertThat(read.getId(), is(73));
    }
}
