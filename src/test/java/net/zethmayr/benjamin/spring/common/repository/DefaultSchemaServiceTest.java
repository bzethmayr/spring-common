package net.zethmayr.benjamin.spring.common.repository;

import lombok.val;
import net.zethmayr.benjamin.spring.common.model.History;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.base.Repository;
import net.zethmayr.benjamin.spring.common.repository.base.RepositoryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.LinkedList;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("ezmode")
public class DefaultSchemaServiceTest {

    @Autowired
    private DefaultSchemaService underTest;

    @SpyBean
    private TestEnumRepository enumRepository;

    @SpyBean
    private TestPojoRepository pojoRepository;

    @TestConfiguration
    static class Overrides {
        @Bean
        @Qualifier("fake")
        public Repository fakeRepo() {
            return mock(Repository.class);
        }
    }

    @Autowired
    @Qualifier("fake")
    private Repository fakeRepo;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        underTest.afterPropertiesSet();
    }

    @After
    public void tearDown() {
        underTest.nuke(enumRepository, pojoRepository);
    }

    @Test
    public void isWired() {
        assertThat(underTest, isA(DefaultSchemaService.class));
    }

    @Test
    public void tablesWereCreated() {
        assertThat(enumRepository.get(1).isPresent(), is(true));
        assertThat(pojoRepository.get(0).isPresent(), is(false));
        verifyZeroInteractions(fakeRepo);
    }

    @Test
    public void canBurnEnums() {
        assertThat(enumRepository.get(1).isPresent(), is(true));
        underTest.burnEnums();
        assertThat(enumRepository.get(1).isPresent(), is(false));
    }

    @Test
    public void canNukeEnums() {
        thrown.expect(RepositoryException.class);
        assertThat(enumRepository.get(1).isPresent(), is(true));
        underTest.nukeEnums();
        enumRepository.get(1);
    }

    private TestPojo fake() {
        return new TestPojo().setWeighting(new BigDecimal("0")).setEvent(History.MAGNA_CARTA).setComment("Overrated");
    }

    @Test
    public void canBurnPojos() {
        val pojo = fake();
        pojoRepository.insert(pojo);
        assertThat(pojoRepository.getMonadic(pojo).isPresent(), is(true));
        underTest.burnPojos();
        assertThat(pojoRepository.getMonadic(pojo).isPresent(), is(false));
    }

    @Test
    public void canNukePojos() {
        thrown.expect(RepositoryException.class);
        val pojo = fake();
        pojoRepository.insert(pojo);
        assertThat(pojoRepository.getMonadic(pojo).isPresent(), is(true));
        underTest.nukePojos();
        pojoRepository.getMonadic(pojo);
    }

    @Test
    public void canBurnAll() {
        pojoRepository.insert(fake());
        assertThat(
                pojoRepository.getAll().size() * enumRepository.getAll().size(),
                greaterThan(0)
        );
        underTest.burnAll();
        assertThat(
                pojoRepository.getAll().size() + enumRepository.getAll().size(),
                is(0)
        );
    }

    @Test
    public void canNukeAll() {
        val thrown = new LinkedList<Throwable>();
        underTest.nukeAll();
        try {
            enumRepository.get(0);
        } catch (RepositoryException re) {
            thrown.add(re);
        }
        try {
            pojoRepository.get(0);
        } catch (RepositoryException re) {
            thrown.add(re);
        }
        assertThat(thrown, hasSize(2));
    }
}
