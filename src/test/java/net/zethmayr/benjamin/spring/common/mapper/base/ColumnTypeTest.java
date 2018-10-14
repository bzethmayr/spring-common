package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.val;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.SecureRandom;
import java.time.Instant;

import static net.zethmayr.benjamin.spring.common.mapper.base.ColumnType.INSTANT;
import static net.zethmayr.benjamin.spring.common.mapper.base.ColumnType.INTEGER;
import static net.zethmayr.benjamin.spring.common.mapper.base.ColumnType.INTEGER_INDEX;
import static net.zethmayr.benjamin.spring.common.mapper.base.ColumnType.LONG;
import static net.zethmayr.benjamin.spring.common.mapper.base.ColumnType.LONG_STRING;
import static net.zethmayr.benjamin.spring.common.mapper.base.ColumnType.SHORT_STRING;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ColumnTypeTest {

    final SecureRandom random = new SecureRandom();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void indexTypesDoNotHavePreparedStatementSetters() {
        thrown.expect(UnsupportedOperationException.class);
        INTEGER_INDEX.setterFactory();
    }

    @Test
    public void evenLongValueTypesHavePreparedStatementSetters() {
        assertThat(LONG.isIndexColumn(), is(false));
        assertThat(LONG.setterFactory(), isA(PsSetterFactory.class));
    }

    @Test
    public void weCanGetADate() {
        assertThat(INSTANT.isIndexColumn(), is(false));
        assertThat(INSTANT.setterFactory(), isA(PsSetterFactory.class));
    }

    @Test
    public void numberLimitsAreIdentity() {
        final int testInt = random.nextInt();
        assertThat(INTEGER_INDEX.limited(testInt), is(testInt));
        assertThat(INTEGER.limited(testInt), is(testInt));
        final long testLong = random.nextLong();
        assertThat(LONG.limited(testLong), is(testLong));
    }

    @Test
    public void shortStringsApplyLimits() {
        final String fourteen = "01234567890123";
        final String fifteen = "012345678901234";
        final String sixteen = "0123456789012345";

        assertThat(SHORT_STRING.limited(null), is(nullValue()));
        assertThat(SHORT_STRING.limited(fourteen), is(fourteen));
        assertThat(SHORT_STRING.limited(fifteen), is(fifteen));
        assertThat(SHORT_STRING.limited(sixteen), is(fifteen));
    }

    @Test
    public void longStringsApplyLimits() {
        final String twofiftyfour;
        final String twofiftyfive;
        final String twofiftysix;
        {
            final String five = "12345";
            final String ten = "1234567890";
            final String fifty = ten + ten + ten + ten + ten;
            final String twohundredfifty = fifty + fifty + fifty + fifty + fifty;
            twofiftyfour = twohundredfifty + "1234";
            twofiftyfive = twohundredfifty + five;
            twofiftysix = twohundredfifty + five + "1";
        }

        assertThat(LONG_STRING.limited(null), is(nullValue()));
        assertThat(LONG_STRING.limited(twofiftyfour), is(twofiftyfour));
        assertThat(LONG_STRING.limited(twofiftyfive), is(twofiftyfive));
        assertThat(LONG_STRING.limited(twofiftysix), is(twofiftyfive));
    }

}
