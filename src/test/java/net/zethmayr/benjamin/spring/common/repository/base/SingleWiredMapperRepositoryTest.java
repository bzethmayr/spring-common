package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.model.TestEnum;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.TestSchemaService;
import net.zethmayr.benjamin.spring.common.repository.TestSingleWiredEnumRepository;
import net.zethmayr.benjamin.spring.common.repository.TestSingleWiredPojoRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SingleWiredMapperRepositoryTest {

    @Autowired
    private TestSingleWiredPojoRepository underTest;

    @Autowired
    private TestSchemaService schema;

    @Before
    public void setUp() {
        schema.applySchemaFor(underTest);
    }

    @After
    public void cleanUp() {
        schema.nuke(underTest);
    }

    @Test
    public void canWriteThenRead() {
        val writ = new TestPojo();
        LOG.debug("writ is {}", writ);
        underTest.insert(writ);
        LOG.debug("writ is {}", writ);
        val read = underTest.getMonadic(writ).orElse(null);
        LOG.debug("read is {}", read);
        assertThat(read, is(writ));
    }
}
