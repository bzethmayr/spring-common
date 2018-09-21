package net.zethmayr.benjamin.spring.common.repository.base;

import net.zethmayr.benjamin.spring.common.mapper.base.EnumRowMapper;
import net.zethmayr.benjamin.spring.common.repository.TestSchemaService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AbstractSchemaServiceMockTest {

    @Autowired
    private TestSchemaService underTest;

    @MockBean
    private JdbcTemplate db;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        reset(db);
    }

    @Test
    public void applySchemaThrowsWhenRepositoryDefinesNoSchema() {
        thrown.expect(IllegalArgumentException.class);
        underTest.applySchemaFor(mock(Repository.class));
    }

    @Test
    public void maybeWriteDataCollectsFailures() {
        final List<Exception> errors = new ArrayList<>();
        underTest.maybeFailToWriteDataFor(mock(EnumRepository.class), errors, null, null);
        assertThat(errors, hasSize(1));
    }

    private EnumRepository enumRepoMockedForWrite() {
        final EnumRepository mockRepo = mock(EnumRepository.class);
        when(mockRepo.mapper()).thenReturn(mock(EnumRowMapper.class));
        return mockRepo;
    }

    @Test
    public void maybeWriteDataSometimesWrites() {
        final List<Exception> errors = new ArrayList<>();
        underTest.maybeFailToWriteDataFor(enumRepoMockedForWrite(), errors, null, null);
        assertThat(errors, hasSize(0));
    }

    @Test
    public void writeDataAppearsToWriteData() {
        underTest.writeDataFor(enumRepoMockedForWrite(), null, null);
        verify(db).execute(startsWith("DELETE"));
        verify(db).batchUpdate(nullable(String.class), any(BatchPreparedStatementSetter.class));
    }

    @Test
    public void writeDataThrowsWhenJdbcThrows() {
        final TransientDataAccessResourceException oops = new TransientDataAccessResourceException("oops");
        thrown.expect(RepositoryException.class);
        thrown.expectCause(is(oops));
        when(db.batchUpdate(nullable(String.class), any(BatchPreparedStatementSetter.class)))
                .thenThrow(oops);
        underTest.writeDataFor(enumRepoMockedForWrite(), null, null);
    }

    @Test
    public void writeDataDoesNotWrapRepositoryException() {
        final RepositoryException oops = RepositoryException.tooManyThings();
        thrown.expect(is(oops));
        thrown.expectCause(nullValue(Throwable.class));
        when(db.batchUpdate(nullable(String.class), any(BatchPreparedStatementSetter.class)))
                .thenThrow(oops);
        underTest.writeDataFor(enumRepoMockedForWrite(), null, null);
    }
}
