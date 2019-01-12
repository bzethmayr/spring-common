package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.val;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.TestPojoRepository;
import net.zethmayr.benjamin.spring.common.repository.TestSchemaService;
import net.zethmayr.benjamin.spring.common.util.ListBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;

import static net.zethmayr.benjamin.spring.common.model.History.DECLARATION_OF_INDEPENDENCE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MapperRepositoryTest {

    @Autowired
    private TestPojoRepository underTest;

    @Autowired
    private TestSchemaService schemaService;

    @SpyBean
    private JdbcTemplate db;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void exists() {
        assertThat(underTest, isA(MapperRepository.class));
    }

    @Before
    public void setUp() {
        schemaService.applySchemaFor(underTest);
    }

    @After
    public void tearDown() {
        reset(db);
        schemaService.nuke(underTest);
    }

    @Test
    public void insertReturnsAQuery() {
        assertThat(underTest.insert(), startsWith("INSERT"));
    }

    @Test
    public void selectReturnsAQuery() {
        assertThat(underTest.select(), startsWith("SELECT"));
    }

    private TestPojo doi() {
        return new TestPojo()
                .setComment("C")
                .setEvent(DECLARATION_OF_INDEPENDENCE)
                .setWeighting(new BigDecimal("0.17"));
    }

    @Test
    public void insertedPojosWithIndexesAreUpdated() {
        val inserted = doi();
        assertThat(inserted.getId(), is(nullValue()));
        val index = underTest.insert(inserted);
        assertThat(inserted.getId(), is(index));
    }

    @Test
    public void canWriteThenRead() {
        val inserted = doi();
        val index = underTest.insert(inserted);
        val read = underTest.get(index).get();
        assertThat(read.getComment(), is(inserted.getComment()));
        assertThat(read.getEvent(), is(inserted.getEvent()));
        assertThat(read.getWeighting(), is(inserted.getWeighting()));
    }

    @Test
    public void canWriteThenDeleteThenNotRead() {
        val foo = doi();
        val index = underTest.insert(foo);
        underTest.delete(index);
        val read = underTest.get(index);
        assertThat(read.isPresent(), is(false));
    }

    private CannotGetJdbcConnectionException oops() {
        return new CannotGetJdbcConnectionException("oops");
    }

    @Test
    public void insertThrowsIfJdbcThrows() {
        thrown.expect(RepositoryException.class);
        val foo = doi();
        doThrow(oops()).when(db).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        underTest.insert(foo);
    }

    @Test
    public void insertThrowsIfItAffectsMoreThanOneRow() {
        thrown.expect(RepositoryException.class);
        doReturn(2).when(db).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        underTest.insert(doi());
    }

    @Test
    public void canDeleteMonadic() {
        val foo = doi();
        val index = underTest.insert(foo);
        underTest.deleteMonadic(foo);
        val read = underTest.get(index);
        assertThat(read.isPresent(), is(false));
    }

    @Test
    public void canGetMonadic() {
        val original = doi();
        val index = underTest.insert(original);
        val copy = new TestPojo().setId(index);
        val read = underTest.getMonadic(copy);
        assertThat(read.isPresent(), is(true));
        assertThat(read.get(), is(original));
    }

    @Test
    public void getThrowsWhenItFindsMoreThanOneThing() {
        thrown.expect(RepositoryException.class);
        thrown.expectMessage(RepositoryException.TOO_MANY_THINGS);
        doReturn(Arrays.asList(null, null))
                .when(db).query(any(), any(RowMapper.class), eq(42));
        underTest.get(42);
    }

    @Test
    public void getRethrowsAnyRepositoryException() {
        val marker = RepositoryException.writeFailed();
        thrown.expect(sameInstance(marker));
        val original = doi();
        val index = underTest.insert(original);
        doThrow(marker).when(db).query(anyString(), any(RowMapper.class), any());
        underTest.get(index);
    }

    @Test
    public void getThrowsIfJdbcThrows() {
        thrown.expect(RepositoryException.class);
        doThrow(oops()).when(db).query(anyString(), any(RowMapper.class), any());
        underTest.get(0);
    }

    @Test
    public void canGetForIndex() {
        val fakeReturn = ListBuilder.array().add(doi(), doi()).build();
        doReturn(fakeReturn).when(db).query(anyString(), any(RowMapper.class), any());
        val returned = underTest.getFor(0);
        assertThat(returned, hasSize(2));
    }

    @Test
    public void getForThrowsWhenJdbcThrows() {
        thrown.expect(RepositoryException.class);
        doThrow(oops()).when(db).query(anyString(), any(RowMapper.class), any());
        underTest.getFor(0);
    }

    @Test
    public void getForRethrowsRepositoryException() {
        val marker = RepositoryException.writeFailed();
        thrown.expect(sameInstance(marker));
        doThrow(marker).when(db).query(anyString(), any(RowMapper.class), any());
        underTest.getFor(0);
    }

    @Test
    public void canGetAll() {
        val fakeReturn = ListBuilder.array().add(doi(), doi()).build();
        doReturn(fakeReturn).when(db).query(anyString(), any(RowMapper.class));
        val returned = underTest.getAll();
        assertThat(returned, hasSize(2));
    }

    @Test
    public void getAllThrowsWhenJdbcThrows() {
        thrown.expect(RepositoryException.class);
        doThrow(oops()).when(db).query(anyString(), any(RowMapper.class));
        underTest.getAll();
    }

    @Test
    public void getAllRethrowsRepositoryException() {
        val marker = RepositoryException.writeFailed();
        thrown.expect(sameInstance(marker));
        doThrow(marker).when(db).query(anyString(), any(RowMapper.class));
        underTest.getAll();
    }

    @Test
    public void canGetUnsafe() {
        val fakeReturn = ListBuilder.array().add(doi()).add(doi()).build();
        doReturn(fakeReturn).when(db).query(anyString(), any(RowMapper.class), any());
        val returned = underTest.getUnsafe(underTest.getById, 0);
        assertThat(returned, hasSize(2));
    }

    @Test
    public void getUnsafeThrowsWhenJdbcThrows() {
        thrown.expect(RepositoryException.class);
        doThrow(oops()).when(db).query(anyString(), any(RowMapper.class), any());
        underTest.getUnsafe(underTest.getById, 0);
    }

    @Test
    public void getUnsafeRethrowsRepositoryException() {
        val marker = RepositoryException.writeFailed();
        thrown.expect(sameInstance(marker));
        doThrow(marker).when(db).query(anyString(), any(RowMapper.class), any());
        underTest.getUnsafe(underTest.getById, 0);
    }

    @Test
    public void canFindAMapper() {
        val found = underTest.findMapper("steve");
        assertThat(found, is(not(nullValue())));
    }

    @Test
    public void cannotFindANonexistentMapper() {
        val found = underTest.findMapper("joe");
        assertThat(found, is(nullValue()));
    }

    @Test
    public void canFindAMapperForOurTable() {
        val found = underTest.findMapper("commentary", "steve");
        assertThat(found, is(not(nullValue())));
    }

    @Test
    public void cannotFindAMapperForNotOurTable() {
        val found = underTest.findMapper("comments", "steve");
        assertThat(found, is(nullValue()));
    }
}
