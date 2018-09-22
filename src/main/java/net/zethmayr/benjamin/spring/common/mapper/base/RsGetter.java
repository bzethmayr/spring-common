package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.ResultSet;

/**
 * Functional interface for methods which
 * extract values from {@link ResultSet}s
 *
 * @param <O> The JDBC field type
 */
@FunctionalInterface
public interface RsGetter<O> {
    /**
     * Retrieves the field value from the resultset.
     *
     * @param rs A resultset
     * @return The field value in the JDBC type
     * @throws MappingException if anything goes wrong
     */
    O from(final ResultSet rs) throws MappingException;
}
