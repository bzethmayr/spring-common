package net.zethmayr.benjamin.spring.common.mapper.base;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

public interface InvertibleRowMapper<T> extends RowMapper<T> {
    /**
     * Returns the row class given at creation.
     *
     * @return the row class given at creation.
     */
    Class<T> rowClass();

    /**
     * Returns the field mappers given at creation.
     *
     * @return All the field mappers
     */
    List<ClassFieldMapper<T>> fields();

    /**
     * Returns the field mappers that are actually used in SELECT queries
     * @return Some of the field mappers
     */
    List<ClassFieldMapper<T>> mappableFields();

    /**
     * Returns the table name given at creation.
     *
     * @return the table name given at creation.
     */
    String table();

    /**
     * Returns the SELECT query from the time of creation.
     *
     * @return the SELECT query from the time of creation.
     */
    String select();

    /**
     * Returns the INSERT query from the time of creation.
     *
     * @return the INSERT query from the time of creation.
     */
    String insert();

    /**
     * Returns a supplier for empty instances of the row class.
     *
     * @return A supplier of new {@link T}
     */
    Supplier<T> empty();

    @Override
    T mapRow(@Nullable ResultSet rs, int i) throws SQLException;

    /**
     * Extracts insert field values from an instance of the row type.
     *
     * @param insert An instance of the row type
     * @return An array of JDBC values
     */
    Object[] getInsertValues(T insert);

    InvertibleRowMapper<T> copyTransforming(final RowMapperTransform mapperTransform, final FieldMapperTransform fieldTransform);

    Mapper<T, ?, ?> idMapper();
}
