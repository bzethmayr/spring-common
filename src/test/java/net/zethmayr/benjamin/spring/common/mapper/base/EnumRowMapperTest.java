package net.zethmayr.benjamin.spring.common.mapper.base;

import net.zethmayr.benjamin.spring.common.model.TestEnum;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

public class EnumRowMapperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void detectsBadSetupWhenQueriesNotCreated() {
        thrown.expect(MappingException.class);
        thrown.expectMessage(MappingException.BAD_SETUP);

        class TestMapper extends EnumRowMapper<TestEnum> {

            private TestMapper() {
                super(TestEnum.class, Collections.emptyList(), null , null, null);
            }
        }
        new TestMapper();
    }
}
