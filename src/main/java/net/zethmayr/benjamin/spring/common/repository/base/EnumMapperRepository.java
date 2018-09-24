package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.extern.slf4j.Slf4j;
import net.zethmayr.benjamin.spring.common.mapper.base.EnumRowMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * This is the default base {@link EnumRepository} implementation, for enums.
 *
 * @param <T> The enum type
 */
@Slf4j
public abstract class EnumMapperRepository<T extends Enum<T>> extends MapperRepository<T, Integer> implements EnumRepository<T> {
    /**
     * Creates a new instance with the specified mapper, using the mapper's id field as the id field.
     *
     * @param jdbcTemplate The jdbc template
     * @param mapper       An enum mapper
     */
    protected EnumMapperRepository(final JdbcTemplate jdbcTemplate, final EnumRowMapper<T> mapper) {
        super(jdbcTemplate, mapper, mapper.idMapper());
    }

    /**
     * For when you're only using one database.
     * <p>
     * Extend this instead of {@link EnumMapperRepository} to autowire your sole JdbcTemplate in
     * {@link InitializingBean#afterPropertiesSet afterPropertiesSet}.
     * {@inheritDoc}
     */
    public abstract static class SingleWired<T extends Enum<T>> extends EnumMapperRepository<T> implements InitializingBean {
        private static JdbcTemplate jdbcTemplate;

        @Autowired
        private EnumMapperRepository.Injector injector;

        public SingleWired(final EnumRowMapper<T> mapper) {
            super(jdbcTemplate, mapper);
        }

        @Override
        public void afterPropertiesSet() {
            ((EnumMapperRepository) this).jdbcTemplate = jdbcTemplate;
        }
    }

    @Service
    public static class Injector {
        public Injector(final @Autowired(required = false) JdbcTemplate jdbcTemplate) {
            SingleWired.jdbcTemplate = jdbcTemplate;
            LOG.debug("Injector was called with jdbcTemplate {}", jdbcTemplate);
        }
    }

    private static final String CANT_DELETE = "You cannot delete enum values.";

    /**
     * Not supported.
     *
     * @param toDelete The index of the object to delete
     */
    @Override
    public void delete(final Integer toDelete) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CANT_DELETE);
    }

    /**
     * Not supported.
     *
     * @param toDelete The object (having the same index as the object) to delete
     */
    @Override
    public void deleteMonadic(final T toDelete) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CANT_DELETE);
    }

    @Override
    public EnumRowMapper<T> mapper() {
        return (EnumRowMapper<T>) mapper;
    }
}
