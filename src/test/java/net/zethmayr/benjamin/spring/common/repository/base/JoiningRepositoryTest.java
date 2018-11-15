package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.val;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.HistoryRepository;
import net.zethmayr.benjamin.spring.common.repository.TestJoiningRepository;
import net.zethmayr.benjamin.spring.common.repository.TestPojoRepository;
import net.zethmayr.benjamin.spring.common.repository.TestSchemaService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static net.zethmayr.benjamin.spring.common.model.History.DECLARATION_OF_INDEPENDENCE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JoiningRepositoryTest {
    @SpyBean
    private TestPojoRepository pojos;

    @SpyBean
    private HistoryRepository enums;

    @Autowired
    private TestSchemaService schemaService;

    @SpyBean
    private JdbcTemplate db;

    @Autowired
    private TestJoiningRepository underTest;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        schemaService.nuke(pojos, enums);
        schemaService.applySchemaFor(pojos, enums);
    }

    private int writeSomeTestDataUsingTheSimpleRepos() {
        val id = pojos.insert(new TestPojo()
                .setComment("Never!")
                .setEvent(DECLARATION_OF_INDEPENDENCE)
                .setSteve(-4));
        enums.insert(DECLARATION_OF_INDEPENDENCE);
        return id;
    }

    @Test
    public void canSelectEntireObject() {
        val id = writeSomeTestDataUsingTheSimpleRepos();
        final TestPojo read = underTest.get(id).orElseThrow(() -> new IllegalStateException("There is too much wrong for this test"));
        assertThat(read, isA(TestPojo.class));
        assertThat(read.getEvent(), is(DECLARATION_OF_INDEPENDENCE));
    }
}
