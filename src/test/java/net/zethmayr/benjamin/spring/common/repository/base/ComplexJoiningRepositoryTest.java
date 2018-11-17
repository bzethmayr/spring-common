package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.model.LinkyPojo;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.LinkyPojoRepository;
import net.zethmayr.benjamin.spring.common.repository.TestPojoRepository;
import net.zethmayr.benjamin.spring.common.repository.TestSchemaService;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
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
                pojos.insert(new TestPojo().setSteve(5).setComment("Huh")),
                pojos.insert(new TestPojo().setSteve(3).setComment("Hey")),
                pojos.insert(new TestPojo().setSteve(2).setComment("That's something")),
                pojos.insert(new TestPojo().setSteve(1).setComment("Darn")),
                pojos.insert(new TestPojo().setSteve(1).setComment("Well.")),
        };
    }

    @Test
    public void canRetrieveLinkedObjects() {
        val ids = insertWeirdTestData();
        val first = underTest.get(ids[0]).orElse(null);
        assertThat(first, isA(LinkyPojo.class));
        assertThat(first.getTop(), isOptional(pojos.get(ids[5])));
        assertThat(first.getLeft(), hasSize(2));
        assertThat(first.getLeft().get(0), isOptional(pojos.get(ids[8])));
        assertThat(first.getLeft().get(1), isOptional(pojos.get(ids[9])));
    }

    private static <T> Matcher<T> isOptional(final Optional<T> optional) {
        return is(optional.orElseThrow(IllegalArgumentException::new));
    }

    @Test
    public void canRetrieveAllWithLinkedObjects() {
        val ids = insertWeirdTestData();
        final List<LinkyPojo> read = underTest.getAll();
        LOG.info("read is {}", read);
        assertThat(read, hasSize(5));
        val first = read.get(0);
        assertThat(first.getTop(), isA(TestPojo.class));
        assertThat(first.getTop(), isOptional(pojos.get(ids[5])));
        assertThat(first.getLeft(), hasSize(2));
        assertThat(first.getLeft().get(0), isOptional(pojos.get(ids[8])));
        assertThat(first.getLeft().get(1), isOptional(pojos.get(ids[9])));
        val second = read.get(1);
        assertThat(second.getTop(), isA(TestPojo.class));
        assertThat(second.getTop(), isOptional(pojos.get(ids[6])));
        assertThat(second.getLeft(), hasSize(2));
        for (int i = 0; i < 2; i++) {
            assertThat(first.getLeft().get(i), is(second.getLeft().get(i)));
        }
        assertThat(first.getTop(), is(not(second.getTop())));
        val third = read.get(2);
        assertThat(third.getTop(), isA(TestPojo.class));
        assertThat(third.getTop(), isOptional(pojos.get(ids[7])));
        assertThat(third.getLeft(), hasSize(1));
        assertThat(third.getLeft().get(0), isOptional(pojos.get(ids[7])));
        assertThat(first.getTop(), is(not(third.getTop())));
        val fourth = read.get(3);
        assertThat(fourth.getTop(), isA(TestPojo.class));
        assertThat(fourth.getTop(), isOptional(pojos.get(ids[8])));
        assertThat(fourth.getLeft(), hasSize(1));
        assertThat(fourth.getLeft().get(0), isOptional(pojos.get(ids[6])));
        assertThat(first.getTop(), is(not(fourth.getTop())));
        val fifth = read.get(4);
        assertThat(fifth.getTop(), isA(TestPojo.class));
        assertThat(fifth.getTop(), isOptional(pojos.get(ids[9])));
        assertThat(fifth.getLeft(), hasSize(1));
        assertThat(fifth.getLeft().get(0), isOptional(pojos.get(ids[5])));
        assertThat(first.getTop(), is(not(fifth.getTop())));
    }

    @Test
    public void canInsertSomeTestData() {
        val ids = insertWeirdTestData();
        final List<LinkyPojo> read = underTest.getAll();
        schemaService.burn(pojos, underTest.primary);
        for (int i = 4; i > 0; i--) {
            underTest.insert(read.get(i));
        }
        final List<LinkyPojo> reread = underTest.getAll();
        LOG.info("reread is {}", reread);
        val last = reread.get(3);
        assertThat(last.getLeft(), hasSize(2));
    }
}
