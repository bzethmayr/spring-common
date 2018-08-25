package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.ResultSet;

@FunctionalInterface
public interface RsGetter<O> {
    O from(final ResultSet rs) throws MappingException;
}
