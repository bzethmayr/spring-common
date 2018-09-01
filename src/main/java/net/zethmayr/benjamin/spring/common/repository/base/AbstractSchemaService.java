package net.zethmayr.benjamin.spring.common.repository.base;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ListBatchPreparedStatementSetter;
import net.zethmayr.benjamin.spring.common.service.Breaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Component
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ, proxyTargetClass = true)
public abstract class AbstractSchemaService {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSchemaService.class);

    @Autowired
    private Breaker breaker;

    private JdbcTemplate db;

    protected void setJdbcTemplate(final JdbcTemplate db) {
        this.db = db;
    }

    private static <T> String createSchemaForStatic(final Repository<T, ?> repository) {
        final StringBuilder sb = new StringBuilder();
        final InvertibleRowMapper<T> rowMapper = repository.mapper();
        if (rowMapper == null || rowMapper.table() == null) {
            throw new IllegalArgumentException("This repository won't take a schema.");
        }
        sb.append("CREATE TABLE IF NOT EXISTS ").append(
                rowMapper.table()
        ).append(" (\n");
        for (ClassFieldMapper field : rowMapper.fields()) {
            sb.append(
                    field.fieldName()
            ).append(" ").append(
                    field.sqlType()
            ).append(", \n");
        }
        sb.setLength(sb.length() - 3);
        sb.append("\n) ;");
        return sb.toString();
    }

    @SafeVarargs
    public final <T extends Enum<T>> void maybeFailToWriteDataFor(
            final EnumRepository<T> enumRepository, final List<Exception> reasons, final T... values
    ) {
        try {
            writeDataFor(enumRepository, values);
        } catch (Exception e) {
            reasons.add(e);
        }
    }

    @SuppressWarnings("unchecked") // because a repository does have the generic type it does have
    public void applySchemaFor(final Repository... repositories) {
        for (Repository repository : repositories) {
            final String schema = createSchemaFor(repository);
            LOG.debug("applying \n{}", schema);
            db.execute(schema);
        }
    }

    public void burn(final Repository... toBurn) {
        for (final Repository repo : toBurn) {
            db.execute("DELETE FROM " + repo.mapper().table());
        }
    }

    public void nuke(final Repository... toNuke) {
        for (final Repository repo : toNuke) {
            db.execute("DROP TABLE IF EXISTS " + repo.mapper().table());
        }
    }

    @Transactional(propagation = REQUIRED, isolation = REPEATABLE_READ, rollbackFor = Throwable.class)
    @SafeVarargs
    public final <T extends Enum<T>> void writeDataFor(final EnumRepository<T> enumRepository, final T... values) {
        try {
            final InvertibleRowMapper<T> mapper = enumRepository.mapper();
            breaker.breaker(1);
            burn(enumRepository);
            final List<T> toInsert = Arrays.asList(values);
            final ListBatchPreparedStatementSetter<T> setter = new ListBatchPreparedStatementSetter<>(toInsert, mapper);
            breaker.breaker(1, 9);
            db.batchUpdate(mapper.insert(), setter);
            breaker.breaker(2);
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception e) {
            throw RepositoryException.writeFailed(e);
        }
    }

    /*
     * All DDL is constructed from constant values.
     * so is all other SQL.
     */
    public <T> String createSchemaFor(final Repository<T, ?> repository) {
        return createSchemaForStatic(repository);
    }
}
