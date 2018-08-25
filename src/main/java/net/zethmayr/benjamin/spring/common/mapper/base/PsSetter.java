package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface PsSetter<O> {
    /**
     * Sets the given value into the given prepared statement.
     * @param ps The prepared statement to set into
     * @param value The value to set
     * @throws MappingException if any failure is detected
     */
    void apply(final PreparedStatement ps, final O value) throws MappingException;
}
