package net.zethmayr.benjamin.spring.common.mapper.base;

import net.zethmayr.benjamin.spring.common.model.base.ModelTrusted;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper.NOT_INDEX;
import static net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper.isIndex;

/**
 * A row mapper.
 *
 * @param <T> The instance type corresponding to a row.
 */
public abstract class InvertibleRowMapper<T> extends ModelTrusted<InvertibleRowMapper> implements RowMapper<T> {

    private final Class<T> rowClass;
    private final List<ClassFieldMapper<T>> fields;
    private final String table;
    private final String selectMappable;
    private final String insert;

    /**
     * Creates a new instance.
     *
     * @param rowClass       The row class
     * @param fields         The field mappers
     * @param table          The table name
     * @param selectMappable A SELECT query to retrieve mappable fields
     * @param insert         An INSERT query to insert specifiable fields
     */
    protected InvertibleRowMapper(final Class<T> rowClass, final List<ClassFieldMapper<T>> fields, final String table, final String selectMappable, final String insert) {
        this.rowClass = rowClass;
        this.fields = Collections.unmodifiableList(fields);
        int i = 1;
        for (ClassFieldMapper<?> m : fields) {
            if (!isIndex(m)) {
                m.setInsertOrdinal(i);
                ++i;
            }
        }
        this.table = table;
        this.selectMappable = selectMappable;
        this.insert = insert;
    }

    /**
     * Returns the row class given at creation.
     *
     * @return {@link #rowClass}
     */
    public final Class<T> rowClass() {
        return rowClass;
    }

    /**
     * Returns the field mappers given at creation.
     *
     * @return {@link #fields}
     */
    public final List<ClassFieldMapper<T>> fields() {
        return fields;
    }

    /**
     * Returns the table name given at creation.
     *
     * @return {@link #table}
     */
    public final String table() {
        return table;
    }

    /**
     * Returns the SELECT query given at creation.
     *
     * @return {@link #selectMappable}
     */
    public final String select() {
        return selectMappable;
    }

    /**
     * Returns the INSERT query given at creation.
     *
     * @return {@link #insert}
     */
    public final String insert() {
        return insert;
    }

    /**
     * Returns an empty instance of the row class.
     *
     * @return A new {@link T}
     */
    public abstract T empty();

    /**
     * Generates a SELECT query for all the mapped fields of the row.
     *
     * @param fields The field mappers
     * @param table  The table name
     * @param <T>    The row type
     * @return A SELECT query with no WHERE clause
     */
    protected static <T> String genSelect(final List<ClassFieldMapper<T>> fields, final String table) {
        return "SELECT " +
                fields.stream()
                        .map(ClassFieldMapper::fieldName)
                        .collect(Collectors.joining(", ")) +
                " FROM " + table;
    }

    /**
     * Generates an INSERT query for all non-generated mapped fields of the row.
     *
     * @param fields The field mappers
     * @param table  The table name
     * @param <T>    The row class
     * @return A parameterized INSERT query
     */
    protected static <T> String genInsert(final List<ClassFieldMapper<T>> fields, final String table) {
        return "INSERT INTO " + table + " (" +
                fields.stream()
                        .filter(NOT_INDEX)
                        .map(ClassFieldMapper::fieldName)
                        .collect(Collectors.joining(", ")) +
                ") VALUES (" +
                fields.stream()
                        .filter(NOT_INDEX)
                        .map(ClassFieldMapper::symbol)
                        .collect(Collectors.joining(", ")) +
                ")";
    }

    @Override
    public T mapRow(final @Nullable ResultSet rs, final int i) throws SQLException {
        final T partial = empty();
        marshaling(partial, true);
        for (ClassFieldMapper<T> m : fields) {
            m.desTo(partial, rs);
        }
        marshaling(partial, false);
        return partial;
    }

    /**
     * Extracts insert field values from an instance of the row type.
     *
     * @param insert An instance of the row type
     * @return An array of JDBC values
     */
    public final Object[] getInsertValues(final T insert) {
        return fields.stream()
                .filter(NOT_INDEX)
                .map((m) -> m.serFrom(insert))
                .toArray();
    }
}
