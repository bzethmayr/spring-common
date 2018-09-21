package net.zethmayr.benjamin.spring.common.repository.base;

import net.zethmayr.benjamin.spring.common.mapper.base.EnumRowMapper;

public interface EnumRepository<T extends Enum<T>> extends Repository<T, Integer> {
    @Override
    EnumRowMapper<T> mapper();
}
