package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.val;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.mapper.base.SqlOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper.NOT_INDEX;
import static net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper.isIndex;

/**
 * This is the default base {@link Repository} implementation, for pojos.
 *
 * @param <T> The persisted object type
 * @param <X> The index type
 */
public abstract class MapperRepository<T, X> implements Repository<T, X> {
    private static final Logger LOG = LoggerFactory.getLogger(MapperRepository.class);

    protected JdbcTemplate jdbcTemplate;

    /**
     * The mapping set at construction.
     */
    public final InvertibleRowMapper<T> mapper;

    /**
     * The INSERT query set at construction.
     */
    public final String insert;

    /**
     * The SELECT query prefix set at construction to retrieve persisted object fields.
     */
    public final String select;

    private final String deleteUnsafe;

    /**
     * The DELETE query set at construction.
     */
    public final String delete;

    /**
     * The mapper used for the id / index field.
     * This can be used to convert index fields of persisted objects.
     */
    public final Mapper<T, ?, X> idMapper;

    /**
     * The SELECT query set at construction for retrieving instances by id / index
     */
    public final String getById;

    /**
     * Constructor accepting the mapper and ID mapper for the repository.
     * Note that the ID mapper need not be the object mapper's ID mapper;
     * this allows multiple repositories over a single SQL database table and mapper.
     *
     * @param jdbcTemplate The jdbc template
     * @param mapper   The object mapper
     * @param idMapper The field mapper for the id / index field
     */
    public MapperRepository(final JdbcTemplate jdbcTemplate, final InvertibleRowMapper<T> mapper, final Mapper<T, ?, X> idMapper) {
        this(jdbcTemplate, mapper, idMapper, " WHERE " + idMapper.fieldName + " = ?");
    }

    private MapperRepository(final JdbcTemplate jdbcTemplate, final InvertibleRowMapper<T> mapper, final Mapper<T, ?, X> idMapper, final String whereId) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        insert = mapper.insert();
        select = mapper.select();
        this.idMapper = idMapper;
        deleteUnsafe = "DELETE FROM " + mapper.table();
        delete = deleteUnsafe + whereId;
        getById = select + whereId;
    }

    private static class Cloned<T, X> extends MapperRepository<T, X> {
        Cloned(JdbcTemplate jdbcTemplate, InvertibleRowMapper<T> mapper, Mapper<T, ?, X> idMapper, final SqlOp relation) {
            super(jdbcTemplate, mapper, idMapper, " WHERE ? " + relation.sql + " " + idMapper.fieldName);
        }
    }

    @Override
    public MapperRepository<T, X> rebindWithRelatedIndex(final SqlOp relation, Mapper<T, ?, X> idMapper) {
        LOG.trace("Rebinding for {} {}", relation, idMapper);
        return new Cloned<T, X>(jdbcTemplate, mapper, idMapper, relation);
    }

    @Override
    public InvertibleRowMapper<T> mapper() {
        return mapper;
    }

    @Override
    public String insert() {
        return insert;
    }

    @Override
    @SuppressWarnings("unchecked") // from m.apply - which may STILL cce if the mapper setup is wrong
    public X insert(final T toInsert) throws ClassCastException {
        try {
            // we expect the id mapper to contribute no value and not be present in sql
            // we ALWAYS use the first mapper as the idMapper here.
            val idMapper = (Mapper<T, ?, X>)mapper.fields().get(0);
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
                insertedCount = jdbcTemplate.update(psc, keys);
                Object key = keys.getKey();
                if (Objects.isNull(key)) {
                    key = jdbcTemplate.queryForObject("SELECT MAX(" + idMapper.fieldName + ") FROM " + mapper.table(), idMapper.getExternalClass());
                    LOG.warn("key not returned - retrieved new maximum from db");
                } else {
                    LOG.trace("we got an ID from the database: {}", key);
                }
                generatedIndex = idMapper.getExternalClass().cast(key);
            } else {
                insertedCount = jdbcTemplate.update(insert, values);
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
        LOG.trace("Deleting {} with {}", delete, toDelete);
        jdbcTemplate.update(delete, toDelete);
    }

    @Override
    public void deleteUnsafe(final String whereClause, final X toDelete) {
        LOG.trace("Deleting {}{} with {}", deleteUnsafe, whereClause, toDelete);
        jdbcTemplate.update(deleteUnsafe + whereClause, toDelete);
    }

    @Override
    public void deleteMonadic(final T toDelete) {
        delete(idMapper.serFrom(toDelete));
    }

    @Override
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
            final List<T> found = jdbcTemplate.query(getById, mapper, id);
            switch (found.size()) {
                case 1:
                    return Optional.of(found.get(0));
                case 0:
                    return Optional.empty();
                default:
                    throw RepositoryException.tooManyThings();
            }
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getFor(final X id) {
        try {
            return jdbcTemplate.query(getById, mapper, id);
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getAll() {
        try {
            return jdbcTemplate.query(select, mapper);
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getUnsafe(final String sql, final Object... values) {
        try {
            return jdbcTemplate.query(sql, mapper, values);
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    /**
     * For when you're only using one database.
     *
     * Extend this instead of {@link MapperRepository} to autowire your sole JdbcTemplate in
     * {@link InitializingBean#afterPropertiesSet afterPropertiesSet}.
     * {@inheritDoc}
     */
    public abstract static class SingleWired<T, X> extends MapperRepository<T, X> implements InitializingBean {
        private static JdbcTemplate jdbcTemplate;

        @Autowired
        private Injector injector;

        public SingleWired(final InvertibleRowMapperBase<T> mapper, final Mapper<T, ?, X> idMapper) {
            super(jdbcTemplate, mapper, idMapper);
        }

        @Override
        public void afterPropertiesSet() {
            ((MapperRepository)this).jdbcTemplate = jdbcTemplate;
        }
    }

    @Service
    public static class Injector {
        public Injector(final @Autowired(required = false) JdbcTemplate jdbcTemplate) {
            SingleWired.jdbcTemplate = jdbcTemplate;
            LOG.debug("Injector was called with jdbcTemplate {}", jdbcTemplate);
        }
    }
}
