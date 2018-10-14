package net.zethmayr.benjamin.spring.common.mapper;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.model.History;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.Arrays;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class HistoryMapperTest {
    private HistoryMapper underTest;

    @Before
    public void setUp() {
        underTest = new HistoryMapper();
    }

    @Test
    public void exists() {
        assertThat(underTest, isA(RowMapper.class));
        assertThat(underTest.table(), is(HistoryMapper.TABLE));
        assertThat(underTest.fields(), is(HistoryMapper.FIELDS));
        assertThat(underTest.rowClass(), sameInstance(History.class));
        assertThat(underTest.insert(), not(isEmptyOrNullString()));
        assertThat(underTest.select(), not(isEmptyOrNullString()));
    }

    @Test
    public void canGenerateInsertValues() {
        val valueToInsert = History.DECLARATION_OF_INDEPENDENCE;
        val toInsert = underTest.getInsertValues(valueToInsert);
        LOG.info("Insert values are {}", Arrays.toString(toInsert));
        assertThat(toInsert, arrayContaining(2, "DECLARATION_OF_INDEPENDENCE", Instant.parse("1776-01-01T00:00:00Z"), 0));
    }

    @Test
    public void canReadFromResultSet() throws Exception {
        val rs = mock(ResultSet.class);
        when(rs.getInt("id")).thenReturn(History.COLUMBUS.ordinal());
        val read = underTest.mapRow(rs, 0);
        assertThat(read, is(History.COLUMBUS));
    }
}
