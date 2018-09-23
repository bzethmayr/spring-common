package net.zethmayr.benjamin.spring.common.repository.base;

import net.zethmayr.benjamin.spring.common.mapper.base.EnumRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This is the default base {@link EnumRepository} implementation, for enums.
 *
 * @param <T> The enum type
 */
public abstract class EnumMapperRepository<T extends Enum<T>> extends MapperRepository<T, Integer> implements EnumRepository<T> {
    /**
     * Creates a new instance with the specified mapper, using the mapper's id field as the id field.
     *
     * @param mapper An enum mapper
     */
    public EnumMapperRepository(final JdbcTemplate jdbcTemplate, final EnumRowMapper<T> mapper) {
        super(jdbcTemplate, mapper, mapper.idMapper());
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
