package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.model.SelfLinkyPojo;
import net.zethmayr.benjamin.spring.common.repository.SelfLinkyPojoRepository;
import net.zethmayr.benjamin.spring.common.repository.TestSchemaService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SelfJoiningRepositoryTest {
    @SpyBean
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestSchemaService schemaService;

    @Autowired
    private SelfLinkyPojoRepository underTest;

    @Before
    public void setUp() {
        schemaService.nuke(underTest.primary);
        schemaService.applySchemaFor(underTest.primary);
    }

    @Test
    public void canInsertThenReadMinimal() throws Exception {
        val toInsert = new SelfLinkyPojo();
        val id = underTest.insert(toInsert);
        val read = underTest.get(id).orElseThrow(Exception::new);
        assertThat(read, is(toInsert));
        assertThat(read.getNeighbors(), hasSize(0));
        assertThat(read.getId(), is(id));
        assertThat(read.getName(), nullValue());
        assertThat(read.getOwners(), hasSize(0));
        assertThat(read.getGroup(), nullValue());
        assertThat(read.getOwned(), nullValue());
        assertThat(read.getOwns(), nullValue());
    }

    @Test
    public void canInsertThenReadWithSelfInGroup() throws Exception {
        val toInsert = new SelfLinkyPojo().setName("foo").setGroup(0);
        val id = underTest.insert(toInsert);
        val read = underTest.get(id).orElseThrow(Exception::new);
        assertThat(read.getId(), is(id));
        assertThat(read.getName(), is("foo"));
        assertThat(read.getNeighbors(), hasSize(1));
        assertThat(read.getNeighbors().get(0), is(toInsert));
    }

    @Test
    public void canInsertSelfOwn() throws Exception {
        val toInsert = new SelfLinkyPojo().setOwns(1);
        val id = underTest.insert(toInsert);
        assertThat(id, is(1));
        val read = underTest.get(id).orElseThrow(Exception::new);
        assertThat(read.getOwners(), hasSize(1));
        assertThat(read.getOwners().get(0).getId(), is(id));
        assertThat(read.getOwned().getId(), is(id));
        assertThat(read.getOwns(), is(id));
    }

    @Test
    public void canHaveFiveBosses() throws Exception {
        val toInsert = new SelfLinkyPojo().setOwners(Arrays.asList(
           new SelfLinkyPojo(), new SelfLinkyPojo(), new SelfLinkyPojo(), new SelfLinkyPojo(), new SelfLinkyPojo()
        ));
        val id = underTest.insert(toInsert);
        val read = underTest.get(id).orElseThrow(Exception::new);
        assertThat(read.getOwners(), hasSize(5));
        assertThat(read.getOwns(), nullValue());
        assertThat(read.getOwners().get(0).getOwns(), is(id));

        val owner = underTest.get(read.getOwners().get(0).getId()).orElseThrow(Exception::new);
        assertThat(owner.getOwned().getId(), is(id));
    }

    @Test
    public void someOwnersMightBeNeighbors() throws Exception {
        val toInsert = new SelfLinkyPojo().setGroup(0).setOwners(Arrays.asList(
                new SelfLinkyPojo().setGroup(0), new SelfLinkyPojo().setGroup(0), new SelfLinkyPojo(), new SelfLinkyPojo(), new SelfLinkyPojo()
        ));
        val id = underTest.insert(toInsert);
        val read = underTest.get(id).orElseThrow(Exception::new);
        assertThat(read.getOwners(), hasSize(5));
        assertThat(read.getOwns(), nullValue());
        assertThat(read.getOwners().get(0).getOwns(), is(id));
        assertThat(read.getNeighbors(), hasSize(3));

        val owner = underTest.get(read.getOwners().get(0).getId()).orElseThrow(Exception::new);
        assertThat(owner.getOwned().getId(), is(id));
        assertThat(owner.getNeighbors(), hasSize(3));
    }
}
