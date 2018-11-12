package net.zethmayr.benjamin.spring.common.mapper.base;

import net.zethmayr.benjamin.spring.common.mapper.HistoryMapper;
import net.zethmayr.benjamin.spring.common.mapper.TestJoiningMapper;
import net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;

import static net.zethmayr.benjamin.spring.common.model.History.COLUMBUS;
import static net.zethmayr.benjamin.spring.common.model.History.MAGNA_CARTA;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JoiningRowMapperTest {
    private TestJoiningMapper underTest;

    private static final String COMMENT = "expected";
    private static final BigDecimal WEIGHT = new BigDecimal("23.01");

    @Before
    public void setUp() {
        underTest = new TestJoiningMapper();
    }

    @Test
    public void canDoStuff() {
        assertThat(underTest, isA(JoiningRowMapper.class));

    }

    @Test
    public void canMapFromRs() throws Exception {
        final ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getInt("0__" + TestPojoMapper.ID.fieldName)).thenReturn(73);
        when(mockRs.getLong("0__"+TestPojoMapper.WEIGHTING.fieldName)).thenReturn(2301L);
        when(mockRs.getString("0__"+TestPojoMapper.COMMENT.fieldName)).thenReturn(COMMENT);
        when(mockRs.getInt("1__"+HistoryMapper.ID.fieldName)).thenReturn(COLUMBUS.ordinal());
        final TestPojo read = underTest.mapRow(mockRs, 0);
        assertThat(read.getComment(), is(COMMENT));
        assertThat(read.getWeighting(), is(WEIGHT));
        assertThat(read.getEvent(), is(COLUMBUS));
        assertThat(read.getId(), is(73));
    }
}
