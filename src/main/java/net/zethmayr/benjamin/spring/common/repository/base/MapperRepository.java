package net.zethmayr.benjamin.spring.common.repository.base;

import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper.NOT_INDEX;
import static net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper.isIndex;

public abstract class MapperRepository<T, X> implements Repository<T, X> {
    private static final Logger LOG = LoggerFactory.getLogger(MapperRepository.class);

    @Autowired
    protected JdbcTemplate emailListJdbc;

    public final InvertibleRowMapper<T> mapper;
    public final String insert;
    public final String select;
    public final String delete;

    public final Mapper<T, ?, X> idMapper;

    public final String getById;

    protected MapperRepository(final InvertibleRowMapper<T> mapper, final Mapper<T, ?, X> idMapper) {
        this.mapper = mapper;
        insert = mapper.insert();
        select = mapper.select();
        this.idMapper = idMapper;
        delete = "DELETE FROM " + mapper.table() + " WHERE " + idMapper.fieldName + " = ?";
        getById = select + " WHERE " + idMapper.fieldName + " = ?";
    }

    @Override
    public InvertibleRowMapper<T> mapper() {
        return mapper;
    }

    public String insert() {
        return insert;
    }

    @SuppressWarnings("unchecked") // from m.apply - which may STILL cce if the mapper setup is wrong
    public X insert(final T toInsert) throws ClassCastException {
        try {
            // we expect the id mapper to contribute no value and not be present in sql
            final Object[] values = mapper.getInsertValues(toInsert);
            LOG.trace("{} {}", insert, Arrays.toString(values));
            final int insertedCount;
            final X generatedIndex;
            if (isIndex(idMapper)) {
                final PreparedStatementCreator psc = (con) -> {
                    final PreparedStatement ps = con.prepareStatement(
                            insert, new int[]{1} // we assume the idMapper to be the first mapper.
                    );
                    mapper.fields().stream()
                            .filter(NOT_INDEX)
                            .map(Mapper.class::cast)
                            .forEach((m) ->
                                m.apply(ps, values[m.getInsertOrdinal() - 1]) // ordinals are SQL 1-based...
                            );
                    return ps;
                };
                final KeyHolder keys = new GeneratedKeyHolder();
                insertedCount = emailListJdbc.update(psc, keys);
                Object key = keys.getKey();
                if (key == null) {
                    key = emailListJdbc.queryForObject("SELECT MAX("+idMapper.fieldName+") FROM "+mapper.table(), idMapper.getExternalClass());
                } else {
                    LOG.trace("we got an ID from the database: {}", key);
                }
                generatedIndex = idMapper.getExternalClass().cast(key);
            } else {
                insertedCount = emailListJdbc.update(insert, values);
                generatedIndex = idMapper.getExternalClass().cast(values[0]);
            }
            if (insertedCount != 1) {
                throw RepositoryException.writeFailed();
            }
            if (isIndex(idMapper)) {
                idMapper.desTo(toInsert, generatedIndex);
            }
            return generatedIndex;
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.writeFailed(e);
        }
    }

    @Override
    public void delete(final X toDelete) {
        emailListJdbc.update(delete, toDelete);
    }

    @Override
    public void deleteMonadic(final T toDelete) {
        delete(idMapper.serFrom(toDelete));
    }

    public String select() {
        return select;
    }

    @Override
    public Optional<T> getMonadic(final T withId) {
        return get(idMapper.serFrom(withId));
    }

    @Override
    public Optional<T> get(final X id) {
        try {
            final List<T> found = emailListJdbc.query(getById, mapper, id);
            switch (found.size()) {
                case 1:
                    return Optional.of(found.get(0));
                case 0:
                    return Optional.empty();
                default:
                    throw RepositoryException.tooManyThings();
            }
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getFor(final X id) {
        try {
            return emailListJdbc.query(getById, mapper, id);
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getAll() {
        try {
            return emailListJdbc.query(select, mapper);
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getUnsafe(final String sql, final Object... values) {
        try {
            return emailListJdbc.query(sql, mapper, values);
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }
}
