package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.mapper.HistoryMapper;
import net.zethmayr.benjamin.spring.common.mapper.TestJoiningMapper;
import net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;

import static net.zethmayr.benjamin.spring.common.model.History.COLUMBUS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class JoiningRowMapperTest {
    private TestJoiningMapper underTest;

    private static final String COMMENT = "expected";
    private static final BigDecimal WEIGHT = new BigDecimal("23.01");

    @Before
    public void setUp() {
        underTest = new TestJoiningMapper();
    }

    @Test
    public void hasGeneratedSelect() {
        assertThat(underTest, isA(JoiningRowMapper.class));
        val select = underTest.select();
        LOG.info("select is {}", select);
        assertThat(select, containsString(underTest.table()));
        assertThat(select, containsString("history"));
    }

    @Test
    public void canMapFromRs() throws Exception {
        final ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getInt("_0__" + TestPojoMapper.ID.fieldName)).thenReturn(73);
        when(mockRs.getLong("_0__"+TestPojoMapper.WEIGHTING.fieldName)).thenReturn(2301L);
        when(mockRs.getString("_0__"+TestPojoMapper.COMMENT.fieldName)).thenReturn(COMMENT);
        when(mockRs.getInt("_1__"+HistoryMapper.ID.fieldName)).thenReturn(COLUMBUS.ordinal());
        when(mockRs.getRow()).thenReturn(1);
        final TestPojo read = underTest.extractor().extractData(mockRs);
        assertThat(read.getComment(), is(COMMENT));
        assertThat(read.getWeighting(), is(WEIGHT));
        assertThat(read.getEvent(), is(COLUMBUS));
        assertThat(read.getId(), is(73));
    }
}
