package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper.prefix;
import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Slf4j
public abstract class JoiningRepository<T, X> implements Repository<T, X> {

    public final JdbcTemplate jdbcTemplate;
    public final JoiningRowMapper<T> mapper;
    public final MapperRepository<T, X> primary;
    public final List<MapperRepository> supplemental;
    public final String getById;

    protected JoiningRepository(final JdbcTemplate jdbcTemplate, final JoiningRowMapper<T> mapper, final MapperRepository<T, X> primary, final MapperRepository... supplemental) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        this.primary = primary;
        this.supplemental = Arrays.asList(supplemental);
        getById = mapper.select() + "\nWHERE " + prefix(0) + "." + primary.idMapper.fieldName + " = ?";
    }

    @Override
    @Transactional(propagation = REQUIRED, isolation = REPEATABLE_READ, rollbackFor = Throwable.class)
    public X insert(T toInsert) {
        val primaryId = primary.insert(toInsert);
        return primaryId;
    }

    @Override
    public void delete(X toDelete) {
        // is this even a good idea?                                never stopped me before...
    }

    @Override
    public void deleteMonadic(T toDelete) {

    }

    @Override
    public String insert() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InvertibleRowMapper<T> mapper() {
        return mapper;
    }

    @Override
    public String select() {
        return mapper.select();
    }

    @Override
    public Optional<T> getMonadic(T withId) {
        return get(primary.idMapper.serFrom(withId));
    }

    @Override
    public Optional<T> get(final X id) {
        try {
            return Optional.of(jdbcTemplate.query(getById, mapper.extractor(), id));
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getFor(X id) {
        try {
            return jdbcTemplate.query(getById, mapper.listExtractor(), id);
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getAll() {
        try {
            return jdbcTemplate.query(mapper.select(), mapper.listExtractor());
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getUnsafe(final String sql, Object... values) {
        try {
            return jdbcTemplate.query(sql, mapper.listExtractor());
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }
}
