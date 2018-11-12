package net.zethmayr.benjamin.spring.common.mapper.base;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface InvertibleRowMapper<T> extends RowMapper<T> {
    /**
     * Returns the row class given at creation.
     *
     * @return {@link #rowClass}
     */
    Class<T> rowClass();

    /**
     * Returns the field mappers given at creation.
     *
     * @return {@link #fields}
     */
    List<ClassFieldMapper<T>> fields();

    /**
     * Returns the table name given at creation.
     *
     * @return {@link #table}
     */
    String table();

    /**
     * Returns the SELECT query given at creation.
     *
     * @return the SELECT query given at creation.
     */
    String select();

    /**
     * Returns the INSERT query given at creation.
     *
     * @return {@link #insert}
     */
    String insert();

    /**
     * Returns an empty instance of the row class.
     *
     * @return A new {@link T}
     */
    T empty();

    @Override
    T mapRow(@Nullable ResultSet rs, int i) throws SQLException;

    /**
     * Extracts insert field values from an instance of the row type.
     *
     * @param insert An instance of the row type
     * @return An array of JDBC values
     */
    Object[] getInsertValues(T insert);

    InvertibleRowMapper<T> copyTransforming(final RowMapperTransform<T> mapperTransform, final FieldMapperTransform<T> fieldTransform);

    Mapper<T, ?, ?> idMapper();
}
