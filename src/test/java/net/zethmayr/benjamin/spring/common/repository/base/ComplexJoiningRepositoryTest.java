package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.val;
import net.zethmayr.benjamin.spring.common.model.LinkyPojo;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.LinkyPojoRepository;
import net.zethmayr.benjamin.spring.common.repository.TestPojoRepository;
import net.zethmayr.benjamin.spring.common.repository.TestSchemaService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ComplexJoiningRepositoryTest {

    @SpyBean
    private TestPojoRepository pojos;

    @Autowired
    private TestSchemaService schemaService;

    @Autowired
    private LinkyPojoRepository underTest;

    @Before
    public void setUp() {
        schemaService.nuke(pojos, underTest.primary);
        schemaService.applySchemaFor(pojos, underTest.primary);
    }

    @Test
    public void wires() {
        assertThat(underTest, isA(LinkyPojoRepository.class));
    }

    private int[] insertWeirdTestData() {
        return new int[]{
                underTest.primary.insert(new LinkyPojo().setLink(1).setName("A name")),
                underTest.primary.insert(new LinkyPojo().setLink(1).setName("Another name")),
                underTest.primary.insert(new LinkyPojo().setLink(2).setName("A new name")),
                underTest.primary.insert(new LinkyPojo().setLink(3).setName("An old name")),
                underTest.primary.insert(new LinkyPojo().setLink(5).setName("A bad name")),
                pojos.insert(new TestPojo().setSteve(1).setComment("Huh")),
                pojos.insert(new TestPojo().setSteve(1).setComment("Hey")),
                pojos.insert(new TestPojo().setSteve(2).setComment("That's something")),
                pojos.insert(new TestPojo().setSteve(3).setComment("Darn")),
                pojos.insert(new TestPojo().setSteve(5).setComment("Well.")),
        };
    }

    @Test
    public void canRetrieveLinkedObjects() {
        val ids = insertWeirdTestData();
        val first = underTest.get(ids[0]).orElse(null);
        assertThat(first, isA(LinkyPojo.class));
        assertThat(first.getTop(), hasSize(1));
        assertThat(first.getLeft(), hasSize(2));
    }

    @Test
    public void canRetrieveAllWithLinkedObjects() {
        val ids = insertWeirdTestData();
        final List<LinkyPojo> read = underTest.getAll();
        assertThat(read, hasSize(5));
    }
}
