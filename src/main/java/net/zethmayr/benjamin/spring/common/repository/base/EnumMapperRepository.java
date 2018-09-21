package net.zethmayr.benjamin.spring.common.repository.base;

import net.zethmayr.benjamin.spring.common.mapper.base.EnumRowMapper;

public abstract class EnumMapperRepository<T extends Enum<T>> extends MapperRepository<T, Integer> implements EnumRepository<T>  {
    public EnumMapperRepository(final EnumRowMapper<T> mapper) {
        super(mapper, mapper.idMapper());
    }

    private static final String CANT_DELETE = "You cannot delete enum values.";

    @Override
    public void delete(final Integer toDelete) {
        throw new UnsupportedOperationException(CANT_DELETE);
    }

    @Override
    public void deleteMonadic(final T toDelete) {
        throw new UnsupportedOperationException(CANT_DELETE);
    }

    @Override
    public EnumRowMapper<T> mapper() {
        return (EnumRowMapper<T>)mapper;
    }
}
